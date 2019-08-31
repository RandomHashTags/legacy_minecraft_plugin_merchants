package me.randomhashtags.merchants;

import me.randomhashtags.merchants.utils.supported.CitizensEvents;
import me.randomhashtags.merchants.utils.supported.economy.Vault;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;

public final class Merchants extends JavaPlugin {
    public static Merchants getPlugin;
    private PluginManager pluginmanager = Bukkit.getPluginManager();
    private MerchantsAPI api;
    public static String factionPlugin;
    public void onEnable() {
        getPlugin = this;
        saveDefaultConfig();
        save(null, "_data.yml");
        File dataF = new File(getDataFolder() + File.separator, "_data.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataF);
        if(data.getBoolean("save default shops")) {
            data.set("save default shops", false);
            try {
                data.save(dataF);
                dataF = new File(getDataFolder() + File.separator, "_data.yml");
                data = YamlConfiguration.loadConfiguration(dataF);
            } catch (Exception e) {
                e.printStackTrace();
            }
            save("shops", "BASE.yml");
            save("shops", "BREWING.yml");
            save("shops", "COLOR.yml");
            save("shops", "DECOR.yml");
            save("shops", "ELIXIR.yml");
            save("shops", "ELIXIRS.yml");
            save("shops", "FARMING.yml");
            save("shops", "FISH.yml");
            save("shops", "HOPPER.yml");
            save("shops", "MISC.yml");
            save("shops", "MOB.yml");
            save("shops", "ORE.yml");
            save("shops", "POTIONS.yml");
            save("shops", "RAID.yml");
        }
        save(null, "shops.yml");

        api = MerchantsAPI.getAPI();
        api.enable();
        getCommand("merchants").setExecutor(api);
        getCommand("sell").setExecutor(api);
        pluginmanager.registerEvents(api, this);

        factionPlugin = pluginmanager.isPluginEnabled("Factions") ? pluginmanager.getPlugin("Factions").getDescription().getAuthors().contains("ProSavage") ? "SavageFactions" : "FactionsUUID" : null;

        if(pluginmanager.isPluginEnabled("Citizens")) {
            CitizensEvents.getCitizensEvents().enable();
        }

        Vault.getVaultAPI().enable(false);
        checkForUpdate();
    }

    public void onDisable() {
        api.disable();
    }

    public void checkForUpdate() {
        try {
            final URL checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=34855");
            final URLConnection con = checkURL.openConnection();
            final String v = getDescription().getVersion(), newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            final boolean canUpdate = !v.equals(newVersion);
            if(canUpdate) {
                final String n = "&6[Merchants] &eUpdate available! &aYour version: &f" + v + "&a. Latest version: &f" + newVersion;
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.isOp() || p.hasPermission("Merchants.updater.notify")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', n));
                    }
                }
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', n));
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Merchants] &aCould not check for updates due to being unable to connect to SpigotMC!"));
        }
    }
    private void save(String folder, String file) {
        File f = null;
        if(folder != null && !folder.equals(""))
            f = new File(getDataFolder() + File.separator + folder + File.separator, file);
        else
            f = new File(getDataFolder() + File.separator, file);
        if(!f.exists()) {
            f.getParentFile().mkdirs();
            saveResource(folder != null && !folder.equals("") ? folder + File.separator + file : file, false);
        }
    }

    public void reload() {
        unload();
        load();
    }
    /*
     * Methods came from the Plugman plugin at
     * https://github.com/r-clancy/PlugMan/blob/master/src/main/java/com/rylinaux/plugman/util/PluginUtil.java
     *
     * Edited by RandomHashTags to fix it not working on servers that don't run Windows, and 1.8.8-1.13.2 support
     */
    private void unload() {
        final Plugin plugin = this;
        String name = plugin.getName();
        SimpleCommandMap commandMap = null;
        List<Plugin> plugins = null;
        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;
        boolean reloadlisteners = true;
        if(pluginmanager != null) {
            pluginmanager.disablePlugin(plugin);
            try {
                Field pluginsField = pluginmanager.getClass().getDeclaredField("plugins");
                pluginsField.setAccessible(true);
                plugins = (List<Plugin>) pluginsField.get(pluginmanager);
                Field lookupNamesField = pluginmanager.getClass().getDeclaredField("lookupNames");
                lookupNamesField.setAccessible(true);
                names = (Map<String, Plugin>) lookupNamesField.get(pluginmanager);
                try {
                    Field listenersField = pluginmanager.getClass().getDeclaredField("listeners");
                    listenersField.setAccessible(true);
                    listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginmanager);
                } catch (Exception e) {
                    reloadlisteners = false;
                }
                Field commandMapField = pluginmanager.getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap) commandMapField.get(pluginmanager);
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commands = (Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        }
        if(plugins != null)
            plugins.remove(plugin);
        if(names != null)
            names.remove(name);
        if(listeners != null && reloadlisteners) {
            for (SortedSet<RegisteredListener> set : listeners.values()) {
                for(Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
                    RegisteredListener value = it.next();
                    if (value.getPlugin() == plugin) {
                        it.remove();
                    }
                }
            }
        }
        if(commandMap != null) {
            for(Iterator<Map.Entry<String, org.bukkit.command.Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, org.bukkit.command.Command> entry = it.next();
                if(entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if(c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }
        // Attempt to close the classloader to unlock any handles on the plugin's jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();
        if(cl instanceof URLClassLoader) {
            try {
                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);
                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ((URLClassLoader) cl).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Will not work on processes started with the -XX:+DisableExplicitGC flag, but lets try it anyway.
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();
    }
    private void load() {
        Plugin target;
        try {
            target = pluginmanager.loadPlugin(new File("plugins" + File.separator + "Merchants.jar"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        target.onLoad();
        pluginmanager.enablePlugin(target);
        return;
    }
}
