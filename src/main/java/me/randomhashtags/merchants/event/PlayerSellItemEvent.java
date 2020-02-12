package me.randomhashtags.merchants.event;

import me.randomhashtags.merchants.addon.MerchantItemObj;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerSellItemEvent extends CancellableEvent {
    public final Player player;
    public final MerchantItemObj item;
    public final ItemStack itemstack;
    public int amount;
    public double revenue;
    public PlayerSellItemEvent(Player player, MerchantItemObj item, ItemStack itemstack, int amount, double revenue) {
        this.player = player;
        this.item = item;
        this.itemstack = itemstack;
        this.amount = amount;
        this.revenue = revenue;
    }
}
