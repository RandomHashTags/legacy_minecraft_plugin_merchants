package me.randomhashtags.merchants.util.obj;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Merchant {
    public static final List<Merchant> merchants = new ArrayList<>();
    public static final HashMap<UUID, Merchant> liveMerchants = new HashMap<>();
    private final String path, commandPermission, npcPermission, opens;
    private final boolean accessibleFromCMD, accessibleFromNPC;
    public Merchant(String path, boolean accessibleFromCMD, String commandPermission, String npcPermission, String opens, boolean accessibleFromNPC) {
        this.path = path;
        this.accessibleFromCMD = accessibleFromCMD;
        this.commandPermission = commandPermission;
        this.npcPermission = npcPermission;
        this.opens = opens;
        this.accessibleFromNPC = accessibleFromNPC;
        merchants.add(this);
    }
    public String getPath() { return path; }
    public boolean isAccessibleFromCMD() { return accessibleFromCMD; }
    public String getCommandPermission() { return commandPermission; }
    public String getNPCPermission() { return npcPermission; }
    public String getOpens() { return opens; }
    public boolean isAccessibleFromNPC() { return accessibleFromNPC; }

    public void spawn(Location l, String name) {
        final NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.setName(name);
        npc.setProtected(true);
        npc.spawn(l);
        liveMerchants.put(npc.getUniqueId(), this);
    }
    public static void despawn(UUID uuid) {
        liveMerchants.remove(uuid);
    }

    public static Merchant valueOf(String path) {
        for(Merchant m : merchants) {
            if(m.getPath().equals(path)) {
                return m;
            }
        }
        return null;
    }
    public static Merchant valueOf(UUID living) {
        return liveMerchants.getOrDefault(living, null);
    }
}
