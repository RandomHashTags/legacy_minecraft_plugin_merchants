package me.randomhashtags.merchants.util;

import me.randomhashtags.merchants.util.universal.UMaterial;
import me.randomhashtags.merchants.util.universal.UVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;

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

    public ItemStack d(FileConfiguration config, String path) {
        item = null;
        if(config == null && path != null || config != null && config.get(path + ".item") != null) {
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
}
