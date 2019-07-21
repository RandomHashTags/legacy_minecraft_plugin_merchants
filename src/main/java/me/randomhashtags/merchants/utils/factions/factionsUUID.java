package me.randomhashtags.merchants.utils.factions;

import com.massivecraft.factions.*;
import org.bukkit.*;
import org.bukkit.event.Listener;
public class factionsUUID implements Listener {
	private static factionsUUID instance;
	public static final factionsUUID getInstance() {
		if(instance == null) instance = new factionsUUID();
		return instance;
	}
	
	private final FPlayers fi = FPlayers.getInstance();
	private final Factions f = Factions.getInstance();
	private final Board b = Board.getInstance();

	public String getFaction(OfflinePlayer player) { return fi.getByOfflinePlayer(player).getFaction().getTag(); }
	public String getFactionAt(Location l) {
		return b.getFactionAt(new FLocation(l)).getTag();
	}
}