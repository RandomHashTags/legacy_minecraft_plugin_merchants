package me.randomhashtags.merchants.util;

import me.randomhashtags.merchants.addon.obj.CustomPotion;
import me.randomhashtags.merchants.universal.UMaterial;
import me.randomhashtags.merchants.universal.UVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MFeature extends UVersion implements Listener {
    private boolean enabled = false, registeredEvents = false;

    public void enable() { enable(true); }
    public void enable(boolean registerEvents) {
        if(!enabled) {
            enabled = true;
            registeredEvents = registerEvents;
            load();
            if(registerEvents) {
                pluginmanager.registerEvents(this, merchants);
            }
        }
    }
    public void disable() {
        if(enabled) {
            enabled = false;
            unload();
            if(registeredEvents) {
                registeredEvents = false;
                HandlerList.unregisterAll(this);
            }
        }
    }
    public boolean isEnabled() { return enabled; }

    public abstract void load();
    public abstract void unload();

    public ItemStack d(YamlConfiguration config, String path) {
        item = null;
        if(config != null && path != null && config.get(path + ".potion") != null) {
            return createCustomPotion(config, path).getItemStack();
        } else if(config == null && path != null || config != null && config.get(path + ".item") != null) {
            if(config != null && config.getString(path + ".item").toLowerCase().contains("spawner") && !config.getString(path + ".item").toLowerCase().startsWith("mob_spawner")) {
                item = UMaterial.SPAWNER.getItemStack(); itemMeta = item.getItemMeta();
                if(config.get(path + ".name") != null) {
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name")));
                    item.setItemMeta(itemMeta);
                }
                return item;
            }
            int amount = config != null && config.get(path + ".amount") != null ? config.getInt(path + ".amount") : 1;
            if(config == null && path.toLowerCase().contains(";amount=")) {
                amount = Integer.parseInt(path.toLowerCase().split(";amount=")[1]);
                path = path.split(";amount=")[0];
            }
            boolean enchanted = config != null && config.getBoolean(path + ".enchanted");
            lore.clear();
            String it = config != null ? config.getString(path + ".item").toUpperCase() : path, name = config != null ? config.getString(path + ".name") : null;
            final String material = it.toUpperCase();
            final UMaterial u;
            try {
                u = UMaterial.match(material);
                item = u.getItemStack();
            } catch (NullPointerException e) {
                Bukkit.broadcastMessage("Unrecognized material: " + material);
                return null;
            }
            final Material skullitem = UMaterial.PLAYER_HEAD_ITEM.getMaterial();
            item.setAmount(amount);
            itemMeta = item.getItemMeta();
            if(item.getType().equals(skullitem) && item.getData().getData() == 3) {
                ((SkullMeta) itemMeta).setOwner(it.split(":").length == 4 ? it.split(":")[3].split("}")[0] : "RandomHashTags");
            } else if(u.name().contains("SPAWN_EGG") && (v.contains("1.9") || v.contains("1.10") || v.contains("1.11") || v.contains("1.12"))) {
                ((org.bukkit.inventory.meta.SpawnEggMeta) itemMeta).setSpawnedType(EntityType.valueOf(u.name().split("_SPAWN_EGG")[0]));
            }
            itemMeta.setDisplayName(name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);

            if(enchanted) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            final HashMap<Enchantment, Integer> enchants = new HashMap<>();
            if(config != null && config.get(path + ".lore") != null) {
                lore.clear();
                for(String string : config.getStringList(path + ".lore")) {
                    if(string.toLowerCase().startsWith("e:"))
                        enchants.put(getEnchantment(string.split(":")[1]), getRemainingInt(string));
                    else if(string.toLowerCase().startsWith("venchants{")) {
                        for(String s : string.split("\\{")[1].split("}")[0].split(";")) {
                            enchants.put(getEnchantment(s), getRemainingInt(s));
                        }
                    } else
                        lore.add(ChatColor.translateAlternateColorCodes('&', string));
                }
            }
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            lore.clear();
            if(enchanted) item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
            for(Enchantment enchantment : enchants.keySet())
                if(enchantment != null)
                    item.addUnsafeEnchantment(enchantment, enchants.get(enchantment));
        }
        return item;
    }
    private CustomPotion createCustomPotion(YamlConfiguration yml, String path) {
        final boolean isSplash = yml.getBoolean(path + ".splash");
        lore.clear();
        final List<PotionEffect> effects = new ArrayList<>();
        for(String p : yml.getStringList(path + ".potion")) {
            final String P = p.toLowerCase();
            if(P.startsWith("effects{")) {
                for(String e : P.split("\\{")[1].split("}")[0].split(";")) {
                    final String[] values = e.split(":");
                    final PotionEffectType t = PotionEffectType.getByName(values[0].toUpperCase());
                    final int lvl = Integer.parseInt(values[1]), duration = Integer.parseInt(values[2]);
                    final PotionEffect pe = new PotionEffect(t, duration*20, lvl);
                    int sec = duration, min = sec/60, hr = min/60;
                    sec -= min*60;
                    min -= hr*60;
                    effects.add(pe);
                    final String tn = t.getName().replace("INCREASE_DAMAGE", "STRENGTH").replace("FAST_DIGGING", "HASTE").replace("HEAL", "INSTANT_HEALTH").replace("HARM", "INSTANT_DAMAGE").replace("JUMP", "JUMP_BOOST").replace("SLOW", "SLOWNESS");
                    String tnn = tn.contains("_") ? tn.replace("_", " ") : tn.substring(0, 1).toUpperCase() + tn.substring(1).toLowerCase();
                    if(tnn.contains(" ")) {
                        String f = tnn.split(" ")[0], l = tnn.split(" ")[1];
                        f = f.substring(0, 1).toUpperCase() + f.substring(1).toLowerCase();
                        l = l.substring(0, 1).toUpperCase() + l.substring(1).toLowerCase();
                        tnn = f + " " + l;
                    }
                    final ChatColor c = tn.contains("HUNGER") || tn.contains("WEAKNESS") || tn.contains("POISON") || tn.contains("SLOW") || tn.contains("WITHER") || tn.contains("CONFUSION") ? ChatColor.RED : ChatColor.GRAY;
                    final String time = !t.getName().equals("HEAL") && !t.getName().equals("HARM") ? " (" + (hr > 0 ? hr + ":" : "") + min + ":" + (sec < 10 ? "0" + sec : sec) + ")" : "";
                    lore.add(c + tnn + " " + toRoman(lvl+1) + time);
                }
            } else {
                lore.add(colorize(p));
            }
        }
        String type = null;
        for(PotionEffect b : effects) {
            if(type == null || type.equals("AWKWARD")) {
                type = potionToString(b.getType());
                item = UMaterial.valueOf((isSplash ? "SPLASH_" : "") + "POTION_" + type).getItemStack();
            }
        }
        itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        final String n = yml.getString(path + ".name");
        if(n != null) {
            itemMeta.setDisplayName(colorize(n));
        }
        itemMeta.setLore(lore); lore.clear();
        item.setItemMeta(itemMeta);
        return new CustomPotion(item.clone(), effects);
    }
}
