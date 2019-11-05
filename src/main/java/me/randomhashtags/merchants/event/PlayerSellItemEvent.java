package me.randomhashtags.merchants.event;

import me.randomhashtags.merchants.util.obj.MerchantItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerSellItemEvent extends CancellableEvent {
    public final Player player;
    public final MerchantItem item;
    public final ItemStack itemstack;
    public int amount;
    public double revenue;
    public PlayerSellItemEvent(Player player, MerchantItem item, ItemStack itemstack, int amount, double revenue) {
        this.player = player;
        this.item = item;
        this.itemstack = itemstack;
        this.amount = amount;
        this.revenue = revenue;
    }
}
