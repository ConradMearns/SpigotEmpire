package com.estrayer.empire;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerUpdater extends BukkitRunnable {
	//Extends BukkitRunnable to create a run method

	EmpirePlugin plugin;

	public PlayerUpdater(EmpirePlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Run every second, update player inventory
	 */
	public void run() {
		for(Player player : Bukkit.getOnlinePlayers()){
			plugin.menuManager.updatePlayerInventory(player);
		}
	}
}
