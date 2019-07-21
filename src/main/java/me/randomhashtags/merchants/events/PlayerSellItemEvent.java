package me.randomhashtags.merchants.events;

import me.randomhashtags.merchants.utils.classes.MerchantItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerSellItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
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
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancel) { cancelled = cancel; }
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
