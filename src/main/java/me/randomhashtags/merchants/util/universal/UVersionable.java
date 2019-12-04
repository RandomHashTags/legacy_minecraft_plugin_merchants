package me.randomhashtags.merchants.util.universal;

import me.randomhashtags.merchants.Merchants;
import me.randomhashtags.merchants.util.Versionable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffectType;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static java.io.File.separator;

public interface UVersionable extends Versionable {
    TreeMap<Integer, String> treemap = new TreeMap<Integer, String>() {{
        put(1000, "M");
        put(900, "CM");
        put(500, "D");
        put(400, "CD");
        put(100, "C");
        put(90, "XC");
        put(50, "L");
        put(40, "XL");
        put(10, "X");
        put(9, "IX");
        put(5, "V");
        put(4, "IV");
        put(1, "I");
    }};
    PluginManager pluginmanager = Bukkit.getPluginManager();
    Merchants merchants = Merchants.getPlugin;
    File dataFolder = merchants.getDataFolder();
    String v = Bukkit.getVersion();
    Server server = Bukkit.getServer();
    ConsoleCommandSender console = Bukkit.getConsoleSender();

    default String toRoman(int number) {
        // This code is from "bhlangonijr" at https://stackoverflow.com/questions/12967896
        if(number <= 0) return "";
        int l = treemap.floorKey(number);
        if(number == l) return treemap.get(number);
        return treemap.get(l) + toRoman(number - l);
    }

    default String potionToString(PotionEffectType a) {
        final String name = a.getName();
        switch (name) {
            case "FIRE_RESISTANCE":
            case "INVISIBILITY":
            case "NIGHT_VISION":
            case "WATER_BREATHING":
            case "WEAKNESS": return name;
            case "HARM": return "HARMING_1";
            case "HEAL": return "HEALING_1";
            case "INCREASE_DAMAGE": return "STRENGTH_1";
            case "JUMP": return "LEAPING_1";
            case "POISON": return "POISON_1";
            case "REGENERATION": return "REGENERATION_1";
            case "SLOW": return "SLOWNESS_1";
            case "SPEED": return "SWIFTNESS_1";
            default: return "AWKWARD";
        }
    }

    default void save(String folder, String file) {
        File f = null;
        if(folder != null && !folder.equals(""))
            f = new File(dataFolder + separator + folder + separator, file);
        else
            f = new File(dataFolder + separator, file);
        if(!f.exists()) {
            f.getParentFile().mkdirs();
            merchants.saveResource(folder != null && !folder.equals("") ? folder + File.separator + file : file, false);
        }
    }


    default double round(double input, int decimals) {
        // From http://www.baeldung.com/java-round-decimal-number
        if(decimals < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(input));
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    default String formatInt(int integer) { return String.format("%,d", integer); }
    default String formatDouble(double d) {
        String decimals = Double.toString(d).split("\\.")[1];
        if(decimals.equals("0")) { decimals = ""; } else { decimals = "." + decimals; }
        return formatInt((int) d) + decimals;
    }

    default int getRemainingInt(String string) {
        string = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string)).replaceAll("\\p{L}", "").replaceAll("\\s", "").replaceAll("\\p{P}", "").replaceAll("\\p{S}", "");
        return string.isEmpty() ? -1 : Integer.parseInt(string);
    }

    default void sendStringListMessage(CommandSender sender, List<String> message, HashMap<String, String> replacements) {
        for(String s : message) {
            if(replacements != null) {
                for(String r : replacements.keySet()) {
                    s = s.replace(r, replacements.get(r));
                }
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
    }

    default Enchantment getEnchantment(String string) {
        if(string != null) {
            string = string.toLowerCase().replace("_", "");
            for(Enchantment enchant : Enchantment.values()) {
                if(enchant != null && string.startsWith(enchant.getName().toLowerCase().replace("_", ""))) {
                    return enchant;
                }
            }
            if(string.startsWith("po")) { return Enchantment.ARROW_DAMAGE; // Power
            } else if(string.startsWith("fl")) { return Enchantment.ARROW_FIRE; // Flame
            } else if(string.startsWith("i")) { return Enchantment.ARROW_INFINITE; // Infinity
            } else if(string.startsWith("pu")) { return Enchantment.ARROW_KNOCKBACK; // Punch
            } else if(string.startsWith("bi") && !EIGHT && !NINE && !TEN) { return Enchantment.getByName("BINDING_CURSE"); // Binding Curse
            } else if(string.startsWith("sh")) { return Enchantment.DAMAGE_ALL; // Sharpness
            } else if(string.startsWith("ba")) { return Enchantment.DAMAGE_ARTHROPODS; // Bane of Arthropods
            } else if(string.startsWith("sm")) { return Enchantment.DAMAGE_UNDEAD; // Smite
            } else if(string.startsWith("de")) { return Enchantment.DEPTH_STRIDER; // Depth Strider
            } else if(string.startsWith("e")) { return Enchantment.DIG_SPEED; // Efficiency
            } else if(string.startsWith("u")) { return Enchantment.DURABILITY; // Unbreaking
            } else if(string.startsWith("firea")) { return Enchantment.FIRE_ASPECT; // Fire Aspect
            } else if(string.startsWith("fr") && !EIGHT) { return Enchantment.getByName("FROST_WALKER"); // Frost Walker
            } else if(string.startsWith("k")) { return Enchantment.KNOCKBACK; // Knockback
            } else if(string.startsWith("fo")) { return Enchantment.LOOT_BONUS_BLOCKS; // Fortune
            } else if(string.startsWith("lo")) { return Enchantment.LOOT_BONUS_MOBS; // Looting
            } else if(string.startsWith("luc")) { return Enchantment.LUCK; // Luck
            } else if(string.startsWith("lur")) { return Enchantment.LURE; // Lure
            } else if(string.startsWith("m") && !EIGHT) { return Enchantment.getByName("MENDING"); // Mending
            } else if(string.startsWith("r")) { return Enchantment.OXYGEN; // Respiration
            } else if(string.startsWith("prot")) { return Enchantment.PROTECTION_ENVIRONMENTAL; // Protection
            } else if(string.startsWith("bl") || string.startsWith("bp")) { return Enchantment.PROTECTION_EXPLOSIONS; // Blast Protection
            } else if(string.startsWith("ff") || string.startsWith("fe")) { return Enchantment.PROTECTION_FALL; // Feather Falling
            } else if(string.startsWith("fp") || string.startsWith("firep")) { return Enchantment.PROTECTION_FIRE; // Fire Protection
            } else if(string.startsWith("pp") || string.startsWith("proj")) { return Enchantment.PROTECTION_PROJECTILE; // Projectile Protection
            } else if(string.startsWith("si")) { return Enchantment.SILK_TOUCH; // Silk Touch
            } else if(string.startsWith("th")) { return Enchantment.THORNS; // Thorns
            } else if(string.startsWith("v") && !EIGHT && !NINE && !TEN) { return Enchantment.getByName("VANISHING_CURSE"); // Vanishing Curse
            } else if(string.startsWith("aa") || string.startsWith("aq")) { return Enchantment.WATER_WORKER; // Aqua Affinity
            } else { return null; }
        }
        return null;
    }

    default List<String> colorizeListString(List<String> input) {
        final List<String> i = new ArrayList<>();
        if(input != null) {
            for(String s : input) {
                i.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }
        return i;
    }
    default String colorize(String input) { return input != null ? ChatColor.translateAlternateColorCodes('&', input) : ""; }

    default void checkForUpdate() {
        try {
            final URL checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=34855");
            final URLConnection con = checkURL.openConnection();
            final String v = merchants.getDescription().getVersion(), newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            final boolean canUpdate = !v.equals(newVersion);
            if(canUpdate) {
                final String n = colorize("&6[Merchants] &eUpdate available! &aYour version: &f" + v + "&a. Latest version: &f" + newVersion);
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p.isOp() || p.hasPermission("Merchants.updater.notify")) {
                        p.sendMessage(n);
                    }
                }
                console.sendMessage(n);
            }
        } catch (Exception e) {
            console.sendMessage(colorize("&6[Merchants] &aCould not check for updates due to being unable to connect to SpigotMC!"));
        }
    }
}
