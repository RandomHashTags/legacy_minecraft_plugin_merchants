package me.randomhashtags.merchants.supported.economy;

import me.randomhashtags.merchants.util.MFeature;
import me.randomhashtags.merchants.supported.Economical;
import me.realized.tokenmanager.TokenManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class TokenManager extends MFeature implements Economical { // https://www.spigotmc.org/resources/8610/
    private static TokenManager instance;
    public static TokenManager getTokenManager() {
        if(instance == null) instance = new TokenManager();
        return instance;
    }

    private TokenManagerPlugin api;

    public void load() {
        api = TokenManagerPlugin.getInstance();
    }
    public void unload() {
    }

    private Player getPlayer(UUID player) { return Bukkit.getPlayer(player); }
    private BigDecimal getTokens(UUID player) { return BigDecimal.valueOf(api.getTokens(getPlayer(player)).getAsLong()); }

    public BigDecimal getBalance(UUID player) { return getTokens(player); }
    public void setBalance(UUID player, BigDecimal bal) { api.setTokens(getPlayer(player), bal.longValue()); }
    public boolean transactionSuccessful(UUID player, BigDecimal amount) {
        final BigDecimal t = getTokens(player);
        final boolean is = t.doubleValue() <= amount.doubleValue();
        if(is) {
            api.setTokens(getPlayer(player), t.subtract(amount).longValue());
        }
        return is;
    }
}
