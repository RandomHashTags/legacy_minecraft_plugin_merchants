package me.randomhashtags.merchants.event;

import org.bukkit.event.Cancellable;

public abstract class CancellableEvent extends AbstractEvent implements Cancellable {
    private boolean cancelled;
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancel) { cancelled = cancel; }
}
