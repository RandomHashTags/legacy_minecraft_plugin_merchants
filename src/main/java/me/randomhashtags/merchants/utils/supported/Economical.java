package me.randomhashtags.merchants.utils.supported;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public interface Economical {
    BigDecimal getBalance(UUID player);
    default BigDecimal getBalance(OfflinePlayer player) { return player != null ? getBalance(player.getUniqueId()) : BigDecimal.ZERO; }
    default BigDecimal getBalance(Player player) { return player != null ? getBalance(player.getUniqueId()) : BigDecimal.ZERO; }

    void setBalance(UUID player, BigDecimal bal);
    default void setBalance(OfflinePlayer player, BigDecimal bal) { setBalance(player.getUniqueId(), bal); }
    default void setBalance(Player player, BigDecimal bal) { setBalance(player.getUniqueId(), bal); }

    boolean transactionSuccessful(UUID player, BigDecimal amount);
    default boolean transactionSuccessful(OfflinePlayer player, BigDecimal amount) { return transactionSuccessful(player.getUniqueId(), amount); }
    default boolean transactionSuccessful(Player player, BigDecimal amount) { return transactionSuccessful(player.getUniqueId(), amount); }
}
