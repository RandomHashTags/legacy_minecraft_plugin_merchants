package me.randomhashtags.merchants.addons;

import me.randomhashtags.merchants.addons.utils.Identifiable;
import me.randomhashtags.merchants.utils.universal.UInventory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;

public interface MerchantShop extends Identifiable {
    boolean isAccessibleFromCommand();
    String getCommandPermission();
    boolean isAccessibleFromNPC();
    String getNPCPermission();

    YamlConfiguration getYaml();
    UInventory getInventory();
    HashMap<Integer, MerchantItem[]> getPages();
}
