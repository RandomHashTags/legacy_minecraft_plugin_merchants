package me.randomhashtags.merchants.addon;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public class MerchantItemObj implements MerchantItem {
    private String identifier, opens;
    private int slot;
    private BigDecimal buyPrice, sellPrice;
    private ItemStack display, purchase;
    private List<String> executedCommands;
    public MerchantItemObj(String identifier, int slot, String opens, BigDecimal buyPrice, BigDecimal sellPrice, ItemStack display, ItemStack purchase, List<String> executedCommands) {
        this.identifier = identifier;
        this.slot = slot;
        this.opens = opens;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.display = display;
        this.purchase = purchase;
        this.executedCommands = executedCommands;
    }
    public String getIdentifier() {
        return identifier;
    }
    public int getSlot() {
        return slot;
    }
    public String getOpensMerchant() {
        return opens;
    }
    public BigDecimal getBuyPrice() {
        return buyPrice;
    }
    public BigDecimal getSellPrice() {
        return sellPrice;
    }
    public ItemStack getItem() {
        return display.clone();
    }
    public ItemStack getPurchased() {
        return purchase != null ? purchase.clone() : null;
    }
    public List<String> getExecutedCommands() {
        return executedCommands;
    }
}
