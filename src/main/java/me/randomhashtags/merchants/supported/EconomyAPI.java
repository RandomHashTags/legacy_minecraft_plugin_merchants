package me.randomhashtags.merchants.supported;

import me.randomhashtags.merchants.util.MFeature;
import me.randomhashtags.merchants.supported.economy.TokenManager;
import me.randomhashtags.merchants.supported.economy.Vault;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class EconomyAPI extends MFeature {
    private static EconomyAPI instance;
    public static EconomyAPI getEconomyAPI() {
        if(instance == null) instance = new EconomyAPI();
        return instance;
    }

    private boolean vault, tokenmanager;
    private HashMap<String, Economical> ecos;

    private boolean isTrue(String path) { return merchants.getConfig().getBoolean(path); }
    public void load() {
        final String p = "supported plugins.economy.";
        vault = isTrue(p + "Vault") && pluginmanager.isPluginEnabled("Vault");
        ecos.put("Vault", Vault.getVaultAPI());
        if(vault) {
            Vault.getVaultAPI().enable(false);
            didHook(pluginmanager.getPlugin("Vault"));
        }

        tokenmanager = isTrue(p + "Token Manager") && pluginmanager.isPluginEnabled("TokenManager");
        if(tokenmanager) {
            TokenManager.getTokenManager().enable(false);
            didHook(pluginmanager.getPlugin("TokenManager"));
        }
    }
    public void unload() {
    }

    private void didHook(Plugin plugin) {
        console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Merchants] &aHooked " + plugin.getName() + " &ev" + plugin.getDescription().getVersion()));
    }

    public boolean hookedVault() { return vault; }
    public boolean hookedTokenManager() { return tokenmanager; }

    public boolean transactionSuccessful(UUID player, BigDecimal amount, EconomyType type) {
        switch (type) {
            case VAULT:
                return Vault.getVaultAPI().transactionSuccessful(player, amount);
            case TOKEN_MANAGER:
                return TokenManager.getTokenManager().transactionSuccessful(player, amount);
            default:
                return false;
        }
    }
}
