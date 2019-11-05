package me.randomhashtags.merchants.addon;

import me.randomhashtags.merchants.util.MerchantStorage;
import me.randomhashtags.merchants.util.universal.UInventory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class FileMerchant implements Merchant {
    private HashMap<Integer, MerchantItem[]> pages;

    public FileMerchant(File f) {

        MerchantStorage.register(this);
    }
    public String getIdentifier() {
        return null;
    }

    public boolean isAccessibleFromCommand() {
        return false;
    }

    public String getCommandPermission() {
        return null;
    }

    public boolean isAccessibleFromNPC() {
        return false;
    }

    public String getNPCPermission() {
        return null;
    }

    public YamlConfiguration getYaml() {
        return null;
    }

    public UInventory getInventory() {
        return null;
    }

    public HashMap<Integer, MerchantItem[]> getPages() {
        if(pages == null) {
            pages = new HashMap<>();
        }
        return pages;
    }
}
