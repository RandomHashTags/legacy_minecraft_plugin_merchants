package me.randomhashtags.merchants.util;

import me.randomhashtags.merchants.addon.Merchant;

import java.util.HashMap;

public abstract class MerchantStorage {
    private static HashMap<String, Merchant> MERCHANTS = new HashMap<>();
    public static void register(Merchant merchant) {
        if(merchant != null) {
            MERCHANTS.put(merchant.getIdentifier(), merchant);
        }
    }
    public static void unregister(Merchant merchant) {
        if(merchant != null) {
            MERCHANTS.remove(merchant.getIdentifier());
        }
    }
}
