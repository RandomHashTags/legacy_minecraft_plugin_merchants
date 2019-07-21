package me.randomhashtags.merchants.utils;

import me.randomhashtags.merchants.MerchantsAPI;
import me.randomhashtags.merchants.utils.classes.Merchant;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class CitizensEvents implements Listener {

    private static CitizensEvents instance;
    public static final CitizensEvents getCitizensEvents() {
        if(instance == null) instance = new CitizensEvents();
        return instance;
    }
    private final MerchantsAPI api = MerchantsAPI.getAPI();

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
        if(m != null && m.isAccessibleFromNPC()) api.viewInventory(event.getClicker(), m.getOpens());
    }
    @EventHandler
    private void npcRightClickEvent(NPCRightClickEvent event) {
        final UUID u = event.getNPC().getUniqueId();
        final Merchant m = Merchant.valueOf(u);
        if(m != null && m.isAccessibleFromNPC()) api.viewInventory(event.getClicker(), m.getOpens());
    }
}
