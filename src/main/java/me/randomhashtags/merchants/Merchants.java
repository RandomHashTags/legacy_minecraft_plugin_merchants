package me.randomhashtags.merchants;

import me.randomhashtags.merchants.supported.CitizensEvents;
import me.randomhashtags.merchants.supported.economy.Vault;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Merchants extends JavaPlugin {
    public static Merchants getPlugin;
    private MerchantsAPI api;
    public static String factionPlugin;

    public void onEnable() {
        getPlugin = this;
        enable();
    }

    public void onDisable() {
        disable();
    }



    public void reload() {
        enable();
        disable();
    }

    public void enable() {
        saveDefaultConfig();
        final PluginManager pluginmanager = Bukkit.getPluginManager();

        api = MerchantsAPI.getAPI();
        api.enable();
        getCommand("merchants").setExecutor(api);
        getCommand("sell").setExecutor(api);

        factionPlugin = pluginmanager.isPluginEnabled("Factions") ? pluginmanager.getPlugin("Factions").getDescription().getAuthors().contains("ProSavage") ? "SavageFactions" : "FactionsUUID" : null;

        if(pluginmanager.isPluginEnabled("Citizens")) {
            CitizensEvents.getCitizensEvents().enable();
        }

        Vault.getVaultAPI().enable(false);
    }
    public void disable() {
        api.disable();
    }
}
