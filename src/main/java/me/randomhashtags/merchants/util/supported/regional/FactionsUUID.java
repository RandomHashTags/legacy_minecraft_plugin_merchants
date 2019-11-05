package me.randomhashtags.merchants.util.supported.regional;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import me.randomhashtags.merchants.util.MFeature;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class FactionsUUID extends MFeature {
	private static FactionsUUID instance;
	public static FactionsUUID getInstance() {
		if(instance == null) instance = new FactionsUUID();
		return instance;
	}
	
	private FPlayers fi;
	private Board b;

	public void load() {
		fi = FPlayers.getInstance();
		b = Board.getInstance();
	}
	public void unload() {
	}

	public String getFaction(OfflinePlayer player) { return fi.getByOfflinePlayer(player).getFaction().getTag(); }
	public String getFactionAt(Location l) {
		return b.getFactionAt(new FLocation(l)).getTag();
	}
}