package me.randomhashtags.merchants.supported.economy;

import me.randomhashtags.merchants.util.MFeature;

public class PlayerPoints extends MFeature { // https://dev.bukkit.org/projects/playerpoints/files
    private static PlayerPoints instance;
    public static PlayerPoints getPlayerPoints() {
        if(instance == null) instance = new PlayerPoints();
        return instance;
    }

    public void load() {
    }
    public void unload() {
    }
}
