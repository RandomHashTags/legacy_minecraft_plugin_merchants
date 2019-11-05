package me.randomhashtags.merchants.util;

import org.bukkit.Bukkit;

public interface Versionable {
    String VERSION = Bukkit.getVersion();
    boolean EIGHT = VERSION.contains("1.8"), NINE = VERSION.contains("1.9"), TEN = VERSION.contains("1.10"), ELEVEN = VERSION.contains("1.11"), TWELVE = VERSION.contains("1.12"), THIRTEEN = VERSION.contains("1.13"), FOURTEEN = VERSION.contains("1.14"), LEGACY = EIGHT || NINE || TEN || ELEVEN || TWELVE;
}
