package com.estrayer.empire;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.estrayer.empire.BuildingManager.Structure;

public class CommandDev implements CommandExecutor{
	
	/**Reference to the plugin so we have access to other methods
	 */
	EmpirePlugin plugin;
	
	/**Used for capturing positions with /dev pos1 and /dev pos2
	 */
	private Location pos1, pos2;
	
	/**This is a programmer defined boolean that should be set to false for releases
	 */
	private final boolean testCommandEnabled = true;
	
	/**
	 * Construct this class from EmpirePlugin
	 * @param plugin
	 */
	public CommandDev(EmpirePlugin plugin){
		this.plugin = plugin;
	}
	
	/**
	 * Break apart all possible uses for the /dev command into seperat functions/methods
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(sender instanceof Player){
			Player player = (Player) sender;
			//Instead of OP, is the player on the devlist?
			if(plugin.getConfig().getBoolean("settings.devlist."+player.getDisplayName())){
				if (args.length == 0) {
					if (player.hasMetadata("isDev")) {
						player.removeMetadata("isDev", plugin);
						player.sendMessage("Dev mode deactivated.");
					} else {
						player.setMetadata("isDev", new FixedMetadataValue(plugin, true));
						player.sendMessage("Dev mode activated.");
					} 
				}else if(player.hasMetadata("isDev")){
					//COMMAND HAS ARGUEMENTS HERE
					if(args.length == 1){
						if(args[0].equalsIgnoreCase("loadConfig")){
							plugin.reloadConfig();
							player.sendMessage("Configuration loaded");
						}
						if(args[0].equalsIgnoreCase("test") && testCommandEnabled){//TODO Test
							player.openInventory(plugin.menuManager.buildMenu);
						}
						
						//Position information
						if(args[0].equalsIgnoreCase("pos1")){
							pos1 = player.getLocation();
							player.sendMessage("Position 1 set");
						}
						if(args[0].equalsIgnoreCase("pos2")){
							pos2 = player.getLocation();
							player.sendMessage("Position 2 set");
						}
					}else if(args.length == 2){
						if(args[0].equalsIgnoreCase("build")){
							plugin.buildingManager.requestBuild(player, args[1]);
						}
						
						if(args[0].equalsIgnoreCase("save")){
							if(pos1 == null || pos2 == null){
								player.sendMessage("You must select two positions first,");
								player.sendMessage("Use [/dev pos1] and [/dev pos2].");
							}else{
								plugin.buildingManager.saveStructure(pos1, pos2, args[1]);
								
								player.sendMessage("Saving structure "+args[1]+" from");
								player.sendMessage(pos1.getBlockX()+", "+pos1.getBlockY()+", "+pos1.getBlockZ()
								+" to " + pos2.getBlockX()+", "+pos2.getBlockY()+", "+pos2.getBlockZ());
							}
						}
						//Teleport
						if(args[0].equalsIgnoreCase("tp")){
							if(plugin.getConfig().contains("empires."+args[1])){
								Location tp = player.getLocation();
								tp.setX(plugin.getConfig().getInt("empires."+args[1]+".location.x"));
								tp.setZ(plugin.getConfig().getInt("empires."+args[1]+".location.y"));
								player.teleport(tp);
							}else{
								player.sendMessage("Player not found in config. Did you include captials?");
							}
						}
						//Get a summary of all buildings a player has
						if(args[0].equalsIgnoreCase("buildings")){
							if(plugin.buildingManager.structures.contains(args[1])){
								String allBuildings = args[1]+"'s buildings: ";
								@SuppressWarnings("unchecked")
								ArrayList<BuildingManager.Structure> list = (ArrayList<Structure>) plugin.buildingManager.structures.get(args[1]);
								list = plugin.buildingManager.getEmpireStructureList(args[1]);
								for(Structure s : list){
									allBuildings += s.type+", ";
								}
								player.sendMessage(allBuildings);
							}else{
								player.sendMessage("Player not found in config. Did you include captials?");
							}
						}
					}
				}else{
					plugin.getLogger().info(player.getDisplayName()+" not in dev mode.");
				}
				return true;
			}else{
				player.sendMessage("Sorry, you don't have permission.");
			}
		}else{
			sender.sendMessage("You must be a player to use that command.");
		}
		return false;
	}
	
	
	
}
