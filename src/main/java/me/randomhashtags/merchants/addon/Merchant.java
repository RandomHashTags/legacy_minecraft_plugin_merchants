package me.randomhashtags.merchants.addon;

import me.randomhashtags.merchants.addon.util.Identifiable;
import me.randomhashtags.merchants.util.universal.UInventory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;

public interface Merchant extends Identifiable {
    boolean isAccessibleFromCommand();
    String getCommandPermission();
    boolean isAccessibleFromNPC();
    String getNPCPermission();

    YamlConfiguration getYaml();
    UInventory getInventory();
    HashMap<Integer, MerchantItem[]> getPages();
}
