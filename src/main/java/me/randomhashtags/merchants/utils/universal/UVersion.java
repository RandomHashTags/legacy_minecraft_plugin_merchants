package me.randomhashtags.merchants.utils.universal;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UVersion {
    public ItemStack item = new ItemStack(Material.APPLE);
    public ItemMeta itemMeta = item.getItemMeta();
    public List<String> lore = new ArrayList<>();


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
}
