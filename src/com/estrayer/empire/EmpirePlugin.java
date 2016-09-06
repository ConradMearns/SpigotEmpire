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
	
	/** Used for information services, like informing a player what the server name is
	 */
	public String SERVER_NAME;
	
	/** The server's main world
	 */
	public World world;
	
	/** Access to building related methods
	 */
	public BuildingManager buildingManager;
	
	/** Access to menu related methods
	 */
	public MenuManager menuManager;
	
	/**
	 * Called when Spigot starts, initializes global variables
	 */
	@Override
	public void onEnable(){
		//Init world
		world = this.getServer().getWorlds().get(0);
		
		//Load configuration and global settings
		loadConfiguration();
		SERVER_NAME = getConfig().getString("settings.server_name");
		buildingManager = new BuildingManager(this);
		menuManager = new MenuManager(this);
		
		//Init Listeners
		getServer().getPluginManager().registerEvents(new LoginListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(menuManager, this);
		
		//Init runnables
		/*BukkitTask playerUpdater = */
		new PlayerUpdater(this).runTaskTimer(this, 20, 20);
		
		//Init Commands Executors
		getCommand("dev").setExecutor(new CommandDev(this));
	}
	
	/**Save our config files before we exit
	 */
	@Override
	public void onDisable(){
		//Save config file
		saveConfig();
		//Save structures
		buildingManager.save();
	}
	
	/**Load the settings configuration and add default settings if they don't already exist
	 */
	public void loadConfiguration(){
		//Set default values, then use if needed
		//Starting resources for new players
		getConfig().addDefault("settings.resources.wood", 256);
		getConfig().addDefault("settings.resources.food", 32);
		getConfig().addDefault("settings.resources.stone", 0);
		getConfig().addDefault("settings.resources.iron", 0);
		getConfig().addDefault("settings.resources.gold", 0);
		
		//Information about how new players are added
		getConfig().addDefault("settings.server_name", "Devland");
		getConfig().addDefault("settings.spawn.radius", 160);
		getConfig().addDefault("settings.spawn.increment.min", 80);
		getConfig().addDefault("settings.spawn.increment.max", 480);
		
		//Dev list, players in this list have access to dev commands
		getConfig().addDefault("settings.devlist.Player", true);
		
		//Structure save locations
		getConfig().addDefault("settings.structures.prefix", "structures"+File.separatorChar);
		getConfig().addDefault("settings.structures.house", "house.yml");
		getConfig().addDefault("settings.structures.town_center", "town_center.yml");
		getConfig().addDefault("settings.structures.barracks", "barracks.yml");
		getConfig().addDefault("settings.structures.university", "university.yml");
		getConfig().addDefault("settings.structures.farm", "farm.yml");
		getConfig().addDefault("settings.structures.mill", "mill.yml");
		
		getConfig().addDefault("settings.structures.stone", "stone.yml");
		getConfig().addDefault("settings.structures.iron", "iron.yml");
		getConfig().addDefault("settings.structures.gold", "gold.yml");
		getConfig().addDefault("settings.structures.tree", "tree.yml");
		
		//Copy defaults, save
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	/** Set starting values for a new player and preform other init tasks
	 * @param player is the new player that we need to give an empire
	 */
	public void initializeNewPlayer(Player player) {	
		//This string defines where information for the player exists
		String confLoc = "empires."+player.getDisplayName();
		
		//Print debug info
		getLogger().info("Making a new player empire for "+player.getDisplayName());
		
		//Greet player
		player.sendMessage("Welcome to "+SERVER_NAME+", Lord "+player.getDisplayName());
		
		//Initialize resources
		int resWood = getConfig().getInt("settings.resources.wood");
		int resFood = getConfig().getInt("settings.resources.food");
		int resStone= getConfig().getInt("settings.resources.stone");
		int resIron = getConfig().getInt("settings.resources.iron");
		int resGold = getConfig().getInt("settings.resources.gold");

		//Gather resources as vars
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
		
		//Set spawn point
		player.setBedSpawnLocation(player.getLocation());
		
		//Build Town Center and starting capital
		buildingManager.build(player, "town_center", player.getLocation());
		
		//Init inventory
		menuManager.initPlayerInventory(player);
		
	}
	
	/**Randomly finds a new location that is far enough away from other players
	 * 
	 * @return Point information, x and y are equal to Bukkit x and z
	 */
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
	
	/**
	 * Get the amount of a particular resource from the player
	 * @param res - String of the resource to get info about
	 * @param player - Player that holds the resource
	 * @return int - amount
	 */
	public int getPlayerResourceAmount(String player, String res){
		if(getConfig().contains("empires."+player)){
			int amt = getConfig().getInt("empires."+player+".resources."+res);
			return amt;
		}else{
			return 0;
		}
		
	}

	/**
	 * Set amount of resource from player
	 * @param player
	 * @param resource
	 */
	public void setPlayerResource(Player player, String resource, int amount){
		getConfig().set("empires."+player.getDisplayName()+".resources."+resource, amount);
	}
	
	/**
	 * Add amount of resource from player
	 * @param player
	 * @param resource
	 */
	public void addPlayerResource(Player player, String resource, int amount){
		amount += getPlayerResourceAmount(player.getDisplayName(), resource);
		getConfig().set("empires."+player.getDisplayName()+".resources."+resource, amount);
	}
	
	/**
	 * Remove amount of resource from player
	 * @param player
	 * @param resource
	 */
	public void takePlayerResource(String player, String resource, int amount){
		amount = getPlayerResourceAmount(player, resource) - amount;
		getConfig().set("empires."+player+".resources."+resource, amount);
	}
}
 