package me.randomhashtags.merchants.addons;

import me.randomhashtags.merchants.addons.utils.Itemable;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public interface MerchantItem extends Itemable {
    MerchantShop getMerchant();
    BigDecimal getBuyPrice();
    BigDecimal getSellPrice();
    ItemStack getPurchased();
    List<String> getExecutedCommands();
}
