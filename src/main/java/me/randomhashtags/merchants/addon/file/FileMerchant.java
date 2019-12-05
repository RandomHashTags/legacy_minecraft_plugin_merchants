package me.randomhashtags.merchants.addon.file;

import me.randomhashtags.merchants.addon.Merchant;
import me.randomhashtags.merchants.addon.MerchantAddon;
import me.randomhashtags.merchants.addon.MerchantItem;
import me.randomhashtags.merchants.addon.obj.MerchantItemObj;
import me.randomhashtags.merchants.util.MerchantStorage;
import me.randomhashtags.merchants.util.universal.UInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;

import static me.randomhashtags.merchants.MerchantsAPI.getAPI;

public class FileMerchant extends MerchantAddon implements Merchant {
    private String cmd;
    private UInventory inv;
    private HashMap<Integer, HashMap<Integer, MerchantItem>> pages;

    private HashMap<String, Boolean> accessibility;
    private HashMap<String, String> perms;

    public FileMerchant(File f) {
        load(f);
        reload();
        MerchantStorage.register(this);
    }
    public void reload() {
        cmd = null;
        inv = null;
        pages = null;
        accessibility = new HashMap<String, Boolean>() {{
            put("cmd", yml.getBoolean("settings.accessibility.cmd"));
            put("npc", yml.getBoolean("settings.accessibility.npc"));
        }};
        perms = new HashMap<String, String>() {{
            put("cmd", yml.getString("settings.permissions.cmd"));
            put("npc", yml.getString("settings.permissions.npc"));
        }};
    }

    public String getIdentifier() { return getYamlName(); }

    public String getCommand() {
        if(cmd == null) cmd = yml.getString("settings.command");
        return cmd;
    }

    public boolean isAccessibleFromCommand() { return accessibility.get("cmd"); }
    public boolean isAccessibleFromNPC() { return accessibility.get("npc"); }

    public String getCommandPermission() { return perms.get("cmd"); }
    public String getNPCPermission() { return perms.get("npc"); }

    public UInventory getInventory() {
        if(inv == null) {
            inv = new UInventory(null, yml.getInt("size"), colorize(yml.getString("title")));
        }
        return inv;
    }

    public HashMap<Integer, HashMap<Integer, MerchantItem>> getPages() {
        if(pages == null) {
            final Inventory inv = getInventory().getInventory();
            pages = new HashMap<>();
            pages.put(1, new HashMap<>());
            int page = 1;
            for(String key : yml.getConfigurationSection("items").getKeys(false)) {
                final String p = "items." + key + ".", opens = yml.getString(p + "opens", null);
                final int slot = yml.getInt(p + "slot");
                final String[] prices = yml.getString("items." + key + ".prices").split(";");
                final BigDecimal buyPrice = BigDecimal.valueOf(Double.parseDouble(prices[0])), sellPrice = BigDecimal.valueOf(Double.parseDouble(prices[1]));
                final ItemStack item = getAPI().d(yml, p), customPurchase = getAPI().d(yml, p + "purchase");
                final MerchantItem merchantItem = new MerchantItemObj(key, slot, opens, buyPrice, sellPrice, item, customPurchase, yml.getStringList(p + "commands"));
                pages.get(page).put(page, merchantItem);

                final ItemMeta meta = item.getItemMeta();
                inv.setItem(slot, item);
            }
        }
        return pages;
    }
}
