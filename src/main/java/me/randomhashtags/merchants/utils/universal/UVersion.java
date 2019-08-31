package me.randomhashtags.merchants.utils.universal;

import me.randomhashtags.merchants.Merchants;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UVersion {
    protected ItemStack item = new ItemStack(Material.APPLE);
    protected ItemMeta itemMeta = item.getItemMeta();
    protected List<String> lore = new ArrayList<>();
    protected final PluginManager pluginmanager = Bukkit.getPluginManager();
    public final Merchants merchants = Merchants.getPlugin;
    protected final String v = Bukkit.getVersion();

    protected void sendStringListMessage(CommandSender sender, List<String> message, HashMap<String, String> replacements) {
        for(String s : message) {
            if(replacements != null) for(String r : replacements.keySet()) s = s.replace(r, replacements.get(r));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
    }

    protected void giveItem(Player player, ItemStack is, int amount) {
        if(is == null || is.getType().equals(Material.AIR)) return;
        is.setAmount(1);
        final World w = player.getWorld();
        final Location l = player.getLocation();
        final PlayerInventory inv = player.getInventory();
        for(int i = 1; i <= amount; i++)
            if(inv.firstEmpty() == -1) w.dropItem(l , is);
            else inv.addItem(is);
        player.updateInventory();
    }
    protected void removeItem(Player player, ItemStack itemstack, int amount) {
        final Inventory inv = player.getInventory();
        int nextslot = getNextSlot(player, itemstack);
        for(int i = 1; i <= amount; i++) {
            if(nextslot >= 0) {
                final ItemStack is = inv.getItem(nextslot);
                if(is.getAmount() == 1) {
                    inv.setItem(nextslot, new ItemStack(Material.AIR));
                    nextslot = getNextSlot(player, itemstack);
                } else {
                    is.setAmount(is.getAmount() - 1);
                }
            }
        }
        player.updateInventory();
    }
    private int getNextSlot(Player player, ItemStack itemstack) {
        final Inventory inv = player.getInventory();
        for(int i = 0; i < inv.getSize(); i++) {
            item = inv.getItem(i);
            if(item != null && item.isSimilar(itemstack)) {
                return i;
            }
        }
        return -1;
    }
    protected int getAvailableAmount(PlayerInventory i, ItemStack is) {
        int a = 0;
        if(is != null && !is.getType().equals(Material.AIR)) {
            final ItemMeta im = is.getItemMeta();
            for(int o = 0; o < i.getSize(); o++) {
                final ItemStack p = i.getItem(o);
                if(p == null)
                    a += is.getMaxStackSize();
                else if(p.getType().equals(is.getType()) && p.getItemMeta().equals(im))
                    a += is.getMaxStackSize()-p.getAmount();
            }
        }
        return a;
    }
    protected int getAmount(PlayerInventory i, ItemStack is) {
        int a = 0;
        if(is != null && !is.getType().equals(Material.AIR)) {
            for(int o = 0; o < i.getSize(); o++) {
                final ItemStack p = i.getItem(o);
                if(p != null && p.isSimilar(is))
                    a += p.getAmount();
            }
        }
        return a;
    }
    // From http://www.baeldung.com/java-round-decimal-number
    public double round(double input, int decimals) {
        if(decimals < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(input));
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    protected String formatInt(int integer) { return String.format("%,d", integer); }
    protected String formatDouble(double d) {
        String decimals = Double.toString(d).split("\\.")[1];
        if(decimals.equals("0")) { decimals = ""; } else { decimals = "." + decimals; }
        return formatInt((int) d) + decimals;
    }

    protected int getRemainingInt(String string) {
        string = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string)).replaceAll("\\p{L}", "").replaceAll("\\s", "").replaceAll("\\p{P}", "").replaceAll("\\p{S}", "");
        return string.equals("") ? -1 : Integer.parseInt(string);
    }

    protected Enchantment getEnchantment(String string) {
        if(string != null) {
            for(Enchantment enchant : Enchantment.values()) {
                if(enchant != null && string.toLowerCase().replace("_", "").startsWith(enchant.getName().toLowerCase().replace("_", ""))) {
                    return enchant;
                }
            }
            string = string.toLowerCase().replace("_", "");
            if(string.startsWith("po")) { return Enchantment.ARROW_DAMAGE; // Power
            } else if(string.startsWith("fl")) { return Enchantment.ARROW_FIRE; // Flame
            } else if(string.startsWith("i")) { return Enchantment.ARROW_INFINITE; // Infinity
            } else if(string.startsWith("pu")) { return Enchantment.ARROW_KNOCKBACK; // Punch
            } else if(string.startsWith("bi") && !(v.contains("1.8")) && !(v.contains("1.9")) && !(v.contains("1.10"))) { return Enchantment.getByName("BINDING_CURSE"); // Blinding Curse
            } else if(string.startsWith("sh")) { return Enchantment.DAMAGE_ALL; // Sharpness
            } else if(string.startsWith("ba")) { return Enchantment.DAMAGE_ARTHROPODS; // Bane of Arthropods
            } else if(string.startsWith("sm")) { return Enchantment.DAMAGE_UNDEAD; // Smite
            } else if(string.startsWith("de")) { return Enchantment.DEPTH_STRIDER; // Depth Strider
            } else if(string.startsWith("e")) { return Enchantment.DIG_SPEED; // Efficiency
            } else if(string.startsWith("u")) { return Enchantment.DURABILITY; // Unbreaking
            } else if(string.startsWith("firea")) { return Enchantment.FIRE_ASPECT; // Fire Aspect
            } else if(string.startsWith("fr") && !(v.contains("1.8"))) { return Enchantment.getByName("FROST_WALKER"); // Frost Walker
            } else if(string.startsWith("k")) { return Enchantment.KNOCKBACK; // Knockback
            } else if(string.startsWith("fo")) { return Enchantment.LOOT_BONUS_BLOCKS; // Fortune
            } else if(string.startsWith("lo")) { return Enchantment.LOOT_BONUS_MOBS; // Looting
            } else if(string.startsWith("luc")) { return Enchantment.LUCK; // Luck
            } else if(string.startsWith("lur")) { return Enchantment.LURE; // Lure
            } else if(string.startsWith("m") && !(v.contains("1.8"))) { return Enchantment.getByName("MENDING"); // Mending
            } else if(string.startsWith("r")) { return Enchantment.OXYGEN; // Respiration
            } else if(string.startsWith("prot")) { return Enchantment.PROTECTION_ENVIRONMENTAL; // Protection
            } else if(string.startsWith("bl") || string.startsWith("bp")) { return Enchantment.PROTECTION_EXPLOSIONS; // Blast Protection
            } else if(string.startsWith("ff") || string.startsWith("fe")) { return Enchantment.PROTECTION_FALL; // Feather Falling
            } else if(string.startsWith("fp") || string.startsWith("firep")) { return Enchantment.PROTECTION_FIRE; // Fire Protection
            } else if(string.startsWith("pp") || string.startsWith("proj")) { return Enchantment.PROTECTION_PROJECTILE; // Projectile Protection
            } else if(string.startsWith("si")) { return Enchantment.SILK_TOUCH; // Silk Touch
            } else if(string.startsWith("th")) { return Enchantment.THORNS; // Thorns
            } else if(string.startsWith("v") && !(v.contains("1.8")) && !(v.contains("1.9")) && !(v.contains("1.10"))) { return Enchantment.getByName("VANISHING_CURSE"); // Vanishing Curse
            } else if(string.startsWith("aa") || string.startsWith("aq")) { return Enchantment.WATER_WORKER; // Aqua Affinity
            } else { return null; }
        }
        return null;
    }
}
