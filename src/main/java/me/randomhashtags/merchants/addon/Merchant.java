package me.randomhashtags.merchants.addon;

import me.randomhashtags.merchants.addon.util.Identifiable;
import me.randomhashtags.merchants.universal.UInventory;
import org.bukkit.Location;

import java.util.HashMap;

public interface Merchant extends Identifiable {
    String getCommand();

    boolean isAccessibleFromCommand();
    String getCommandPermission();
    boolean isAccessibleFromNPC();
    String getNPCPermission();

    UInventory getInventory();
    HashMap<Integer, HashMap<Integer, MerchantItem>> getPages();

    default void spawn(Location l, String name) {
        // TODO: complete dis bruv
    }
}
