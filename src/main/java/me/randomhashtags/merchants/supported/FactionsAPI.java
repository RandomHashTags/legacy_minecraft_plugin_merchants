package me.randomhashtags.merchants.supported;

import me.randomhashtags.merchants.supported.regional.FactionsUUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import static me.randomhashtags.merchants.Merchants.factionPlugin;

public class FactionsAPI {
	private static FactionsAPI instance;
	public static FactionsAPI getFactionsAPI() {
		if(instance == null) instance = new FactionsAPI();
		return instance;
	}

	public String getFaction(OfflinePlayer player) {
		if(factionPlugin == null)                   return "";
		else if(factionPlugin.contains("Factions")) return FactionsUUID.getInstance().getFaction(player);
		else return "";
	}
	public String getFactionAt(Location l) {
		if(factionPlugin == null) return "";
		else if(factionPlugin.contains("Factions")) return FactionsUUID.getInstance().getFactionAt(l);
		else return null;
	}
}
