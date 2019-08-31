package me.randomhashtags.merchants.utils;

import me.randomhashtags.merchants.utils.universal.UVersion;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class MFeature extends UVersion implements Listener {
    private boolean enabled = false, registeredEvents = false;

    public void enable() { enable(true); }
    public void enable(boolean registerEvents) {
        if(!enabled) {
            enabled = true;
            registeredEvents = registerEvents;
            load();
            if(registerEvents) {
                pluginmanager.registerEvents(this, merchants);
            }
        }
    }
    public void disable() {
        if(enabled) {
            enabled = false;
            unload();
            if(registeredEvents) {
                registeredEvents = false;
                HandlerList.unregisterAll(this);
            }
        }
    }
    public boolean isEnabled() { return enabled; }

    public abstract void load();
    public abstract void unload();
}
