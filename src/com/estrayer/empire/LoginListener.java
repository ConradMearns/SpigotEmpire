package com.estrayer.empire;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class LoginListener implements Listener{
	
	EmpirePlugin plugin;
	
	//Constructor so we can get plugin info
	public LoginListener(EmpirePlugin plugin){
		this.plugin = plugin;
	}
	
	/**
	 * Whenever a player joins for the first time, initialize an empire
	 * @param event - PlayerJoinEvent
	 */
	@EventHandler
	public void newLoginListener(PlayerJoinEvent event){
		//Test to see if that player has ever joined before
		
		//Retrieve player name
		String playerName = event.getPlayer().getDisplayName();
		
		if(plugin.getConfig().getString("empires."+playerName) == null){
			//Initialize new player
			plugin.initializeNewPlayer(event.getPlayer());
		}else{
			//Old player joins server, do something?
		}
	}
	
}
