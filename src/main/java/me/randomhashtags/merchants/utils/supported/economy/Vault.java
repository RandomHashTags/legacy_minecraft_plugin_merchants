package me.randomhashtags.merchants.utils.supported.economy;

import me.randomhashtags.merchants.utils.MFeature;
import me.randomhashtags.merchants.utils.supported.Economical;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.UUID;

public class Vault extends MFeature implements Economical {
	private static Vault instance;
	public static Vault getVaultAPI() {
		if(instance == null) instance = new Vault();
		return instance;
	}
	
	private Economy economy = null;
	public void load() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if(economyProvider != null) {
			economy = economyProvider.getProvider();
		}
	}
	public void unload() {
	}

    private OfflinePlayer getPlayer(UUID player) { return Bukkit.getOfflinePlayer(player); }
    private BigDecimal getBal(UUID player) { return economy != null ? BigDecimal.valueOf(economy.getBalance(getPlayer(player))) : BigDecimal.ZERO; }

    public BigDecimal getBalance(UUID player) { return getBal(player); }
    public void setBalance(UUID player, BigDecimal bal) {
		if(economy != null) {
			final OfflinePlayer p = getPlayer(player);
			economy.withdrawPlayer(p, getBal(player).doubleValue());
			economy.depositPlayer(p, bal.doubleValue());
		}
	}
	public boolean transactionSuccessful(UUID player, BigDecimal amount) { return economy != null && economy.withdrawPlayer(getPlayer(player), amount.doubleValue()).transactionSuccess(); }
}
