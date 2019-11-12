package me.randomhashtags.merchants.util;

public final class CommandManager {
    private static CommandManager instance;
    public static CommandManager getCommandManager() {
        if(instance == null) instance = new CommandManager();
        return instance;
    }
}
