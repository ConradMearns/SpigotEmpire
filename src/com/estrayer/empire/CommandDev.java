package com.estrayer.empire;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class CommandDev implements CommandExecutor{
	
	EmpirePlugin plugin;
	
	private Location pos1;
	private Location pos2;
	
	private boolean testCommandEnabled = false;
	
	public CommandDev(EmpirePlugin plugin){
		this.plugin = plugin;
	}
	
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
						if(args[0].equalsIgnoreCase("test") && testCommandEnabled){
							plugin.buildingManager.buildInstantly(BuildingManager.HOUSE, player.getLocation());
							player.sendMessage("Test executed");
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
						//Builds
						if(args[0].equalsIgnoreCase("build")){
							String prefix = plugin.getConfig().getString("settings.structures.prefix");
							String fileLocation = prefix + args[1] + ".yml";
							plugin.buildingManager.buildInstantly(fileLocation, player.getLocation());
						}
						
						if(args[0].equalsIgnoreCase("save")){
							String prefix = plugin.getConfig().getString("settings.structures.prefix");
							String fileLocation = prefix + args[1] + ".yml";
							if(pos1 == null || pos2 == null){
								player.sendMessage("You must select two positions first,");
								player.sendMessage("Use [/dev pos1] and [/dev pos2].");
							}else{
								plugin.buildingManager.saveStructureBetween(pos1, pos2, fileLocation);
								
								player.sendMessage("Saveing structure "+args[1]+".yml from");
								player.sendMessage(pos1.getBlockX()+", "+pos1.getBlockY()+", "+pos1.getBlockZ()
								+" to " + pos2.getBlockX()+", "+pos2.getBlockY()+", "+pos2.getBlockZ());
							}
						}
						
						if(args[0].equalsIgnoreCase("tp")){
							if(plugin.getConfig().contains("empires."+args[1])){
								Location tp = player.getLocation();
								tp.setX(plugin.getConfig().getInt("empires."+args[1]+".location.x"));
								tp.setZ(plugin.getConfig().getInt("empires."+args[1]+".location.y"));
								player.teleport(tp);
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
