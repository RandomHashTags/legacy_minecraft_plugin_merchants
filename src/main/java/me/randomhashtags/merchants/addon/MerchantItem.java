package me.randomhashtags.merchants.addon;

import me.randomhashtags.merchants.addon.util.Itemable;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public interface MerchantItem extends Itemable {
    Merchant getMerchant();
    BigDecimal getBuyPrice();
    BigDecimal getSellPrice();
    ItemStack getPurchased();
    List<String> getExecutedCommands();
}
