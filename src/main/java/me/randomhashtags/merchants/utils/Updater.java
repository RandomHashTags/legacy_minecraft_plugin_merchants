package me.randomhashtags.merchants.utils;

import me.randomhashtags.merchants.Merchants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Updater {

    private static Updater instance;
    public static Updater getUpdater() {
        if(instance == null) instance = new Updater();
        return instance;
    }
    public boolean updateIsAvailable = true;

    public void checkForUpdate() {
        try {
            final URL checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 34855);
            final URLConnection con = checkURL.openConnection();
            final String v = Merchants.getPlugin.getDescription().getVersion(), newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            final boolean canUpdate = !v.equals(newVersion);
            updateIsAvailable = canUpdate;
            if(canUpdate) {
                final String n = "&6[Merchants] &eUpdate available! &aYour version: &f" + v + "&a. Latest version: &f" + newVersion;
                for(Player p : Bukkit.getOnlinePlayers())
                    if(p.isOp() || p.hasPermission("Merchants.updater.notify"))
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', n));
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', n));
            }
        } catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Merchants] &aCould not check for updates due to being unable to connect to SpigotMC!"));
        }
    }
}
