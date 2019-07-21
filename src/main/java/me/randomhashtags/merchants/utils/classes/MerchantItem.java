package me.randomhashtags.merchants.utils.classes;

import me.randomhashtags.merchants.utils.universal.UMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MerchantItem {
    private static final HashMap<String, List<MerchantItem>> items = new HashMap<>();
    private static final HashMap<UMaterial, MerchantItem> memory = new HashMap<>();
    public final String merchant, path, opens;
    public final int slot;
    public final double buyPrice, sellPrice;
    private final ItemStack display, purchase;
    public final List<String> executedCommands;
    public MerchantItem(String merchant, String path, int slot, String opens, double buyPrice, double sellPrice, ItemStack display, ItemStack purchase, List<String> executedCommands) {
        this.merchant = merchant;
        this.path = path;
        this.slot = slot;
        this.opens = opens;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.display = display;
        this.purchase = purchase;
        this.executedCommands = executedCommands;
        if(!items.containsKey(merchant)) items.put(merchant, new ArrayList<>());
        items.get(merchant).add(this);
    }
    public ItemStack getDisplay() { return display.clone(); }
    public ItemStack getPurchase() { return purchase != null ? purchase.clone() : null; }

    public static MerchantItem valueOf(String merchant, int slot) {
        for(MerchantItem m : items.get(merchant))
            if(m.slot == slot)
                return m;
        return null;
    }
    public static MerchantItem valueOf(String merchant, String path) {
        for(MerchantItem m : items.get(merchant))
            if(m.path.equals(path))
                return m;
        return null;
    }
    public static MerchantItem valueOf(ItemStack is) {
        if(is != null && !is.getType().equals(Material.AIR)) {
            final UMaterial u = UMaterial.match(is);
            if(memory.keySet().contains(u)) return memory.get(u);
            final Material t = u.getMaterial();
            final byte d = u.getData();
            for(String s : items.keySet()) {
                for(MerchantItem m : items.get(s)) {
                    final ItemStack dis = m.getDisplay();
                    if(dis.getType().equals(t) && dis.getData().getData() == d) {
                        memory.put(u, m);
                        return m;
                    }
                }
            }
        }
        return null;
    }
}
