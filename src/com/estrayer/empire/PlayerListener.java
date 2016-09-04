package com.estrayer.empire;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener{
	
	//Instance of plugin
	EmpirePlugin plugin;
	
	PhantomStructureManager psm;
	
	/**Constructor
	 * @param plugin - Main EmpirePlugin
	 */
	public PlayerListener(EmpirePlugin plugin){
		this.plugin = plugin;
		psm = new PhantomStructureManager(plugin);
	}
	
	//TODO make this run always instead
	@EventHandler(priority = EventPriority.HIGH)
	public void updatePlayerLookingAt(PlayerMoveEvent event){
		Player player = event.getPlayer();
		
		//If the player is in building mode, then we need to know where they are looking at
		
		if(player.hasMetadata("isBuilding")){
			if(player.getMetadata("isBuilding").get(0).asBoolean() && player.hasMetadata("toBuild")){
				String toBuild = player.getMetadata("toBuild").get(0).asString();
				psm.updatePhantoms(player, toBuild);
			}else if(!player.getMetadata("isBuilding").get(0).asBoolean()){
				psm.deletePhantoms(player);
			}
		}		
	}
	
}
