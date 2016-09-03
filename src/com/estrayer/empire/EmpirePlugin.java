package com.estrayer.empire;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EmpirePlugin extends JavaPlugin{
	
	public String SERVER_NAME;
	public World world;
	
	public BuildingManager buildingManager;
	
	@Override
	public void onEnable(){
		world = this.getServer().getWorlds().get(0);
		//Load configuration
		loadConfiguration();
		SERVER_NAME = getConfig().getString("settings.server_name");
		
		//Listeners
		getServer().getPluginManager().registerEvents(new LoginListener(this), this);
		
		//Commands
		getCommand("dev").setExecutor(new CommandDev(this));
		
		buildingManager = new BuildingManager(this);
	}
	
	@Override
	public void onDisable(){
		saveConfig();
		//TODO Save structures
	}

	public void loadConfiguration(){
		//Set default values, then use if needed
		getConfig().addDefault("settings.resources.wood", 256);
		getConfig().addDefault("settings.resources.food", 32);
		getConfig().addDefault("settings.resources.stone", 0);
		getConfig().addDefault("settings.resources.iron", 0);
		getConfig().addDefault("settings.resources.gold", 0);
		
		getConfig().addDefault("settings.server_name", "Devland");
		getConfig().addDefault("settings.spawn.radius", 160);
		getConfig().addDefault("settings.spawn.increment.min", 80);
		getConfig().addDefault("settings.spawn.increment.max", 480);
		
		getConfig().addDefault("settings.devlist.Player", true);
		
		getConfig().addDefault("settings.structures.prefix", "structures"+File.separatorChar);
		getConfig().addDefault("settings.structures.house", "house.yml");
		getConfig().addDefault("settings.structures.town_center", "town_center.yml");
		getConfig().addDefault("settings.structures.barracks", "barracks.yml");
		getConfig().addDefault("settings.structures.university", "university.yml");
		getConfig().addDefault("settings.structures.farm", "farm.yml");
		getConfig().addDefault("settings.structures.mill", "mill.yml");
		//Copy defaults, save
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public void initializeNewPlayer(Player player) {	
		String confLoc = "empires."+player.getDisplayName();

		getLogger().info("Making a new player empire for "+player.getDisplayName());
		
		//Greet player
		player.sendMessage("Welcome to "+SERVER_NAME+", Lord "+player.getDisplayName());
		//Initialize resources
		//Gather resources as vars
		int resWood = getConfig().getInt("settings.resources.wood");
		int resFood = getConfig().getInt("settings.resources.food");
		int resStone= getConfig().getInt("settings.resources.stone");
		int resIron = getConfig().getInt("settings.resources.iron");
		int resGold = getConfig().getInt("settings.resources.gold");
		getConfig().set(confLoc+".resources.wood", resWood);
		getConfig().set(confLoc+".resources.food", resFood);
		getConfig().set(confLoc+".resources.stone", resStone);
		getConfig().set(confLoc+".resources.iron", resIron);
		getConfig().set(confLoc+".resources.gold", resGold);
		//Set to adventure mode
		player.setGameMode(GameMode.ADVENTURE);
		//Teleport to starting location
		Point position = findNewRandomLocation();
		int height = player.getWorld().getHighestBlockYAt(position.x, position.y);
		player.teleport(new Location(player.getWorld(), position.x, height, position.y));
		getLogger().info("Sending "+player.getDisplayName()+" to X: "+position.x+", "+position.y);
		//Save that location
		getConfig().set(confLoc+".location.x", position.x);
		getConfig().set(confLoc+".location.y", position.y);
		//Build Town Center and starting capital
		
	}
	
	public Point findNewRandomLocation(){
		int minRadius = getConfig().getInt("settings.spawn.radius");
		int minIncrement = getConfig().getInt("settings.spawn.increment.min");
		int maxIncrement = getConfig().getInt("settings.spawn.increment.max");
		
		ArrayList<Point> locations = new ArrayList<Point>();
		
		//Collect list of all current empire locations
		for(String key : getConfig().getConfigurationSection("empires").getKeys(false)){
			Point p = new Point();
			p.x = getConfig().getInt(key+"spawn.x");
			p.y = getConfig().getInt(key+"spawn.y");
			locations.add(p);
		}
		
		Random random = new Random();
		random.setSeed(System.currentTimeMillis());
		
		//Start at origin
		boolean useX = random.nextBoolean();
		boolean positive = random.nextBoolean();
		
		//Loop until position is safe
		boolean safe = false;
		Point currentPosition = new Point(0,0);
		while(!safe){
			//Determine how we want to move
			useX = random.nextBoolean();
			positive = random.nextBoolean();
			//Determine how far
			int increment = random.nextInt(maxIncrement-minIncrement)+minIncrement;
			//Update the current position
			int posMod = 0;
			if(positive){
				posMod++;
			}else{
				posMod--;
			}
			if(useX){
				currentPosition.x += (posMod*increment);
			}else{
				currentPosition.y += (posMod*increment);
			}
			//Test to see if this location is safe
			//assume distanceSafe to be true unless we prove otherwise
			boolean distanceSafe = true;
			for(Point point : locations){
				int distance = (int) point.distance(currentPosition);
				//If we find that a single point is too close, we arent safe
				if(distance < minRadius){
					distanceSafe = false;
				}
			}
			//After checking every new spawn location, distanceSafe will
			//only be true if it is far away enough
			safe = distanceSafe;
		}
		//We now know a safe position
		return currentPosition;
	}
	
}
 