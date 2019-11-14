package me.randomhashtags.merchants.addon;

import me.randomhashtags.merchants.addon.util.Identifiable;
import me.randomhashtags.merchants.util.universal.UInventory;

import java.util.HashMap;

public interface Merchant extends Identifiable {
    String getCommand();

    boolean isAccessibleFromCommand();
    String getCommandPermission();
    boolean isAccessibleFromNPC();
    String getNPCPermission();

    UInventory getInventory();
    HashMap<Integer, MerchantItem[]> getPages();
}
