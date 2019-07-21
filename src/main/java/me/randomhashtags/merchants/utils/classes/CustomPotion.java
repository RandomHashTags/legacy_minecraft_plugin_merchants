package me.randomhashtags.merchants.utils.classes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class CustomPotion {
    private static final List<CustomPotion> custompotions = new ArrayList<>();
    private final ItemStack is;
    private final List<PotionEffect> potioneffects;
    public CustomPotion(ItemStack is, List<PotionEffect> potioneffects) {
        this.is = is;
        this.potioneffects = potioneffects;
        custompotions.add(this);
    }
    public ItemStack getItemStack() { return is.clone(); }
    public List<PotionEffect> getPotionEffects() { return potioneffects; }

    public static CustomPotion valueOf(ItemStack is) {
        if(is != null && is.hasItemMeta())
            for(CustomPotion cp : custompotions) {
                if(cp.getItemStack().getItemMeta().equals(is.getItemMeta()))
                    return cp;
            }
        return null;
    }
}
