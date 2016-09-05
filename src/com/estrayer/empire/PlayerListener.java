package com.estrayer.empire;

import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

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
	
	/**
	 * If the player is in build mode, we need to update where phantoms are located
	 * @param event
	 */
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
	
	/**
	 * Listen to actions from the player. If they are in build mode, either cancel a build, or complete one
	 * @param event
	 */
	@EventHandler
	public void playerBuildStructureAction(PlayerInteractEvent event){
		Player player = event.getPlayer();
		
		if(player.getInventory().getItemInMainHand().getItemMeta() != null
				&& player.getInventory().getItemInMainHand().hasItemMeta()){
			ItemMeta im = player.getInventory().getItemInMainHand().getItemMeta();
			
			if(im.getDisplayName().equalsIgnoreCase("build")){
				
				//We know what to build
				if(player.hasMetadata("isBuilding") && player.hasMetadata("toBuild")){
					Action action = event.getAction();
					
					int NONE = 0;
					int LEFT = 1;
					int RIGHT = 2;
					
					int lastBuildAction = NONE;
					
					if(!player.hasMetadata("lastBuildAction")){
						player.setMetadata("lastBuildAction", new FixedMetadataValue(plugin, NONE));
					}else{
						lastBuildAction = player.getMetadata("lastBuildAction").get(0).asInt();
					}
					
					if(action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)){
						if(lastBuildAction == LEFT){
							//BUILD!
							String toBuild = player.getMetadata("toBuild").get(0).asString();
							Location loc = player.getTargetBlock((Set<Material>)null, 30).getLocation();
							loc.setY(loc.getY()+1);
							plugin.buildingManager.build(player, toBuild, loc, 5, true);
							
							//Reset build mode			
							plugin.buildingManager.disableBuildMode(player);
							psm.deletePhantoms(player);
						}else{
							player.sendMessage("Left click again to confirm.");
							player.setMetadata("lastBuildAction", new FixedMetadataValue(plugin, LEFT));
						}
					}
					if(action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)){
						if(lastBuildAction == RIGHT){
							//Reset build mode
							plugin.buildingManager.disableBuildMode(player);
							player.sendMessage("Canclled build");
						}else{
							player.sendMessage("Right click again to cancel.");
							player.setMetadata("lastBuildAction", new FixedMetadataValue(plugin, RIGHT));
						}
						
					}
				}else{//Open a menu to build something
					player.openInventory(plugin.menuManager.buildMenu);
				}
			}
		}
				
	}
	
	/**
	 * Prevent players from dropping menu items
	 * @param event
	 */
	@EventHandler
	public void drop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode().equals(GameMode.ADVENTURE)){
			event.setCancelled(true);
		}
	}
	
	/**
	 * Ensure players never starve to death
	 * @param event
	 */
	@EventHandler
	public void noHunger(FoodLevelChangeEvent event){
		event.setCancelled(true);
	}
	
	@EventHandler
	public void noEating(PlayerItemConsumeEvent event){
		event.setCancelled(true);
	}
	
}
