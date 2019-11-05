package me.randomhashtags.merchants.util.supported;

import me.randomhashtags.merchants.util.MFeature;
import me.randomhashtags.merchants.util.obj.Merchant;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

import static me.randomhashtags.merchants.MerchantsAPI.getAPI;

public final class CitizensEvents extends MFeature implements Listener {
    private static CitizensEvents instance;
    public static CitizensEvents getCitizensEvents() {
        if(instance == null) instance = new CitizensEvents();
        return instance;
    }

    public void load() {
    }
    public void unload() {
    }

    @EventHandler
    private void npcDespawnEvent(NPCDespawnEvent event) {
        final DespawnReason reason = event.getReason();
        if(!reason.equals(DespawnReason.PENDING_RESPAWN) && !reason.name().contains("UNLOAD") && !reason.equals(DespawnReason.RELOAD)) {
            final UUID u = event.getNPC().getUniqueId();
            Merchant.despawn(u);
        }
    }
    @EventHandler
    private void npcLeftClickEvent(NPCLeftClickEvent event) {
        final UUID u = event.getNPC().getUniqueId();
        final Merchant m = Merchant.valueOf(u);
        if(m != null && m.isAccessibleFromNPC()) {
            getAPI().viewInventory(event.getClicker(), m.getOpens());
        }
    }
    @EventHandler
    private void npcRightClickEvent(NPCRightClickEvent event) {
        final UUID u = event.getNPC().getUniqueId();
        final Merchant m = Merchant.valueOf(u);
        if(m != null && m.isAccessibleFromNPC()) {
            getAPI().viewInventory(event.getClicker(), m.getOpens());
        }
    }
}
