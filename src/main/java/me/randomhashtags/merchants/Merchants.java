package me.randomhashtags.merchants;

import me.randomhashtags.merchants.util.supported.CitizensEvents;
import me.randomhashtags.merchants.util.supported.economy.Vault;
import me.randomhashtags.merchants.util.universal.UVersionable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public final class Merchants extends JavaPlugin implements UVersionable {
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

    public void checkForUpdate() {
        try {
            final URL checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=34855");
            final URLConnection con = checkURL.openConnection();
            final String v = getDescription().getVersion(), newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            final boolean canUpdate = !v.equals(newVersion);
            if(canUpdate) {
                final String n = ChatColor.translateAlternateColorCodes('&', "&6[Merchants] &eUpdate available! &aYour version: &f" + v + "&a. Latest version: &f" + newVersion);
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.isOp() || p.hasPermission("Merchants.updater.notify")) {
                        p.sendMessage(n);
                    }
                }
                console.sendMessage(n);
            }
        } catch (Exception e) {
            console.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Merchants] &aCould not check for updates due to being unable to connect to SpigotMC!"));
        }
    }

    public void reload() {
        enable();
        disable();
    }

    public void enable() {
        saveDefaultConfig();
        save(null, "_data.yml");
        File dataF = new File(getDataFolder() + File.separator, "_data.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataF);
        if(data.getBoolean("save default shops")) {
            data.set("save default shops", false);
            try {
                data.save(dataF);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final String[] shops = new String[] {
                    "BASE", "BREWING", "COLOR", "DECOR",
                    "ELIXIR", "ELIXERS", "FARMING", "FISH",
                    "HOPPER", "MISC", "MOB", "ORE", "POTIONS",
                    "RAID"
            };
            for(String s : shops) {
                save("shops", s + ".yml");
            }
        }
        save(null, "shops.yml");

        api = MerchantsAPI.getAPI();
        api.enable();
        getCommand("merchants").setExecutor(api);
        getCommand("sell").setExecutor(api);

        factionPlugin = pluginmanager.isPluginEnabled("Factions") ? pluginmanager.getPlugin("Factions").getDescription().getAuthors().contains("ProSavage") ? "SavageFactions" : "FactionsUUID" : null;

        if(pluginmanager.isPluginEnabled("Citizens")) {
            CitizensEvents.getCitizensEvents().enable();
        }

        Vault.getVaultAPI().enable(false);
        checkForUpdate();
    }
    public void disable() {
        api.disable();
    }
}
