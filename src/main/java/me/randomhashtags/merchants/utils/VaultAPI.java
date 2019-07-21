package me.randomhashtags.merchants.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultAPI {
	
	private static VaultAPI instance;
	public static final VaultAPI getVaultAPI() {
		if(instance == null) instance = new VaultAPI();
		return instance;
	}
	
	public static Economy economy = null;
	public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(economyProvider != null) { economy = economyProvider.getProvider(); } return (economy != null);
    }
}
