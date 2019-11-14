package me.randomhashtags.merchants.addon;

import me.randomhashtags.merchants.util.MerchantStorage;
import me.randomhashtags.merchants.util.universal.UInventory;

import java.io.File;
import java.util.HashMap;

public class FileMerchant extends MerchantAddon implements Merchant {
    private File f;
    private String cmd;
    private UInventory inv;
    private HashMap<Integer, MerchantItem[]> pages;

    private HashMap<String, Boolean> accessibility;
    private HashMap<String, String> perms;

    public FileMerchant(File f) {
        this.f = f;
        reload();
        MerchantStorage.register(this);
    }
    public void reload() {
        load(f);
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

    public HashMap<Integer, MerchantItem[]> getPages() {
        if(pages == null) {
            pages = new HashMap<>();
        }
        return pages;
    }
}
