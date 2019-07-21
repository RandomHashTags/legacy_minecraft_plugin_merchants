package me.randomhashtags.merchants.utils;

import me.randomhashtags.merchants.utils.factions.factionsUUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;

import static me.randomhashtags.merchants.Merchants.factionPlugin;

public class FactionsAPI implements Listener {
	
	private static FactionsAPI instance;
	public static final FactionsAPI getFactionsAPI() {
		if(instance == null) instance = new FactionsAPI();
		return instance;
	}

	public String getFaction(OfflinePlayer player) {
		if(factionPlugin == null)                   return "";
		else if(factionPlugin.contains("Factions")) return factionsUUID.getInstance().getFaction(player);
		else return "";
	}
	public String getFactionAt(Location l) {
		if(factionPlugin == null) return "";
		else if(factionPlugin.contains("Factions")) return factionsUUID.getInstance().getFactionAt(l);
		else return null;
	}
}
