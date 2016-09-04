package com.estrayer.empire;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class BuildingManager {
	
	/**
	 * Reference to the plugin so we have access to other methods
	 */
	EmpirePlugin plugin;
	
	/**
	 * Stores file location prefix
	 */
	private String prefix;
	
	/**
	 * Where the sctructures config is saved and accessed from
	 */
	private File structuresFile;
	
	/**
	 * This is the structures config that stores metadata about where buildings have been placed
	 */
	public FileConfiguration structures;
	
	/**
	 * Initialize this class
	 * @param plugin is to set the global reference variable
	 */
	public BuildingManager(EmpirePlugin plugin){
		this.plugin = plugin;
		
		//Init prefix
		prefix = plugin.getConfig().getString("settings.structures.prefix");
		
		//Init configuration
		structuresFile = new File(plugin.getDataFolder(), "structures.yml");
		structures = YamlConfiguration.loadConfiguration(structuresFile);
		
	}
	
	/**
	 * Test to see if proposed structure is outside of all other structure's perimeters 
	 * @param proposedStruct - This is tested against all other structures
	 */
	public boolean permissionToBuild(Structure proposedStruct){
		
		boolean permission = true;
		
		//loop through all players within structure.yml
		for(String playerName: structures.getConfigurationSection("").getKeys(false)){
			
			//loop through each players structures and place the structure strings into an array
			ArrayList<Structure> empireStructures= getEmpireStructureList(playerName);
			
			// compare each object's xy and r to proposed building
			for(Structure structure: empireStructures){
				int dz = proposedStruct.z_loc-structure.z_loc;
				int dx = proposedStruct.x_loc-structure.x_loc;
				
				if(Math.sqrt((dz)^2+(dx)^2)<(proposedStruct.radius+structure.radius)){
					plugin.getLogger().info(Math.sqrt((dz)^2+(dx)^2)+ " "+(proposedStruct.radius+structure.radius));
					//if any structure conflicts return false
					permission = false;
					break;
				}
			}
			// stop the entire process if permission is not given
			if (permission == false){
				break;
			}
			
		}
		return permission;
	}
	
	/**
	 * Build a structure into the world
	 * @param p who wants to build
	 * @param name of structure template
	 * @param loc of structure placement
	 */
	public void build(Player p, String name, Location loc){
		this.build(p, name, loc, 0);
	}
	
	/**
	 * Build a structure into the world
	 * @param p who wants to build
	 * @param name of structure template
	 * @param loc of structure placement
	 * @param speed of block by block placement
	 */
	public void build(Player p, String name, Location loc, int speed){
		this.build(p, name, loc, speed, false);
	}
	
	/**
	 * Build a structure into the world
	 * @param p who wants to build
	 * @param name of structure template
	 * @param loc of structure placement
	 * @param speed of block by block placement
	 * @param sound to toggle sound effects
	 */
	public void build(Player p, String name, Location loc, int speed, boolean sound){
		//Create an array of blocks
		ArrayList<Block> blocks = getStructureBlockMeta(name);
		
		//Grab structure metadata and init values
		Structure struct = getStructureMeta(name);
		struct.type = name;
		struct.x_loc = loc.getBlockX();
		struct.z_loc = loc.getBlockZ();
		struct.y_loc = loc.getBlockY();
		
		//Shuffle the array so the blocks are added with a random effect
		Collections.shuffle(blocks);
		
		if (permissionToBuild(struct)) {
			//Debug info
			plugin.getLogger().info(
					"Built structure at " + blocks.get(0).x + ", " + blocks.get(0).y + ", " + blocks.get(0).z + ", ");
			//Loop through ever block and create an asynchronous task to build block by block
			int index = 0;
			for (Block b : blocks) {
				index++;
				new BukkitRunnable() {

					@Override
					public void run() {
						placeBlock(b.id, b.data, loc.getBlockX() + b.x, loc.getBlockY() + b.y, loc.getBlockZ() + b.z);
						if (sound) {
							plugin.world.playSound(loc, Sound.BLOCK_STONE_PLACE, 1F, 1F);
						}
					}

				}.runTaskLater(this.plugin, speed * index);

			}
			//Save the structure
			saveStructureMeta(struct, p);
		}else{
			p.sendMessage("You cannot build structures on other structures, please change your location and try again");
		}
	}
	
	/**
	 * Place a block at position x,y,z and initialize it's id and data
	 * @param id
	 * @param data
	 * @param x
	 * @param y
	 * @param z
	 */
	@SuppressWarnings("deprecation")
	public void placeBlock(int id, byte data, int x, int y, int z){
		plugin.world.getBlockAt(x, y, z).setTypeId(id);
		plugin.world.getBlockAt(x, y, z).setData(data);
	}

	/**
	 * Get a list of structures that an empire has built
	 * @param empire - The name of the player who has buildings
	 * @return an array of Structure metadata
	 */
	public ArrayList<Structure> getEmpireStructureList(String empire){
		//Init an empty list
		ArrayList<Structure> structureList = new ArrayList<Structure>();
		
		//Init and set a list of structures as strings
		@SuppressWarnings("unchecked")
		ArrayList<String> stringList = (ArrayList<String>) plugin.buildingManager.structures.get(empire);
		
		//Loop through every structure string and convert it into meta. Save that meta
		for(String string : stringList){
			Structure structure = new Structure();
			structure.setFromString(string);
			structureList.add(structure);
			
		}
		
		return structureList;
	}
	
	/**
	 * Save a structure template given a position
	 * @param pos1 - The first grabbed world position
	 * @param pos2 - The second grabbed world position
	 * @param name - The name of this structure template
	 */
	@SuppressWarnings("deprecation")
	public void saveStructure(Location pos1, Location pos2, String name) {
		//Find difference between pos1 and pos2 and save as int values
		int x,y,z;
		int lgX,lgY,lgZ;
		
		Location offsetRadius = pos1;
		
		if(pos1.getBlockX() > pos2.getBlockX()){
			lgX = pos1.getBlockX();
			x = pos2.getBlockX();
		}else{
			lgX = pos2.getBlockX();
			x = pos1.getBlockX();
		}
		
		if(pos1.getBlockY() > pos2.getBlockY()){
			lgY = pos1.getBlockY();
			y = pos2.getBlockY();
		}else{
			lgY = pos2.getBlockY();
			y = pos1.getBlockY();
		}
		
		if(pos1.getBlockZ() > pos2.getBlockZ()){
			lgZ = pos1.getBlockZ();
			z = pos2.getBlockZ();
		}else{
			lgZ = pos2.getBlockZ();
			z = pos1.getBlockZ();
		}
		
		//Use some arithmetic to determine a relative center
		offsetRadius.setX((lgX-x+1)/2);
		offsetRadius.setZ((lgZ-z+1)/2);
		if(offsetRadius.getBlockX() > offsetRadius.getBlockZ()){
			offsetRadius.setY(offsetRadius.getBlockX());
		}else{
			offsetRadius.setY(offsetRadius.getBlockZ());
		}
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		//loop through every value between var and dvar, inclusive;
		for(int ix=x; ix <= lgX; ix++){
			for(int iy=y; iy <= lgY; iy++){
				for(int iz=z; iz <= lgZ; iz++){
					//plugin.getLogger().info("ix: "+ix + " iy: "+iy+" iz: "+iz);
					//plugin.getLogger().info("lgx: "+lgX + " lgy: "+lgY+" lgz: "+lgZ);
					if(plugin.world.getBlockAt(ix, iy, iz).getTypeId() != 0){
						//Not Air
						Block block = new Block();
						block.x = (ix-x);
						block.y = (iy-y);
						block.z = (iz-z);
						block.id = plugin.world.getBlockAt(ix, iy, iz).getTypeId();
						block.data = plugin.world.getBlockAt(ix, iy, iz).getData();
						blocks.add(block);
						
						if(block.id == 0){
							plugin.getLogger().info("Air detected...");
						}
					}
				}
			}
		}
		//We now have an ArrayList of blocks, pass this to a config saving method
		saveStructureBlockMeta(blocks, name, offsetRadius);
	}
	
	/**
	 * Save MetaData of a newly built structure
	 * @param structure metadata that has been built
	 * @param player that has built the structure
	 */
	public void saveStructureMeta(Structure structure, Player player){
		//Grab list of all buildings as strings
		@SuppressWarnings("unchecked")
		ArrayList<String> buildings = (ArrayList<String>) structures.getList(player.getDisplayName());
		
		//If this list is null, we need to initiate it so we can start adding content
		if(buildings == null){
			buildings = new ArrayList<String>();
		}
		
		//Serialize our structure into a string and add it to the list
		buildings.add(structure.toString());
		
		//Overwrite the structures config entry to include our new building
		structures.set(player.getDisplayName(), buildings);
		
		//Try to save
		try {
			structures.save(structuresFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Acquire metadata from a structure template
	 * @param name is the name of a structure, where the location is defined by main config entry 
	 * @return the metadata as a Structure
	 */
	public Structure getStructureMeta(String name){
		//Construct a filepath based on the main config entry for 'name'
		String filepath = prefix + plugin.getConfig().getString("settings.structures."+name);
		
		//Create a file and config for this structure
		File configFile;
		FileConfiguration config;
		
		//Init configFile and config so that we can access them
		configFile = new File(plugin.getDataFolder(), filepath);
		config = YamlConfiguration.loadConfiguration(configFile);
		
		//Init a new Structure
		Structure struct = new Structure();
		
		//Init individual variables with the Structure
		struct.x_size = config.getInt("origin.x");
		struct.z_size = config.getInt("origin.z");
		struct.radius = config.getInt("radius");
		
		return struct;
	}

	/**
	 * After collecting an array of blocks, serialize them into a new file
	 * @param blocks - list of blocks to save
	 * @param name - the name under the main config that points to where to save
	 * @param origin - Used to store origin information. The Y value is used to store a radius
	 */
	public void saveStructureBlockMeta(ArrayList<Block> blocks, String name, Location origin){
		//Warning! Y-value in location is used for radius!
		String filepath = prefix + plugin.getConfig().getString("settings.structures."+name);
		
		plugin.getLogger().info("Saving "+blocks.size()+" block to "+filepath);
		
		File configFile;
		FileConfiguration config;
		
		configFile = new File(plugin.getDataFolder(), filepath);
		config = YamlConfiguration.loadConfiguration(configFile);
		
		//Save a default value for an origin offset and radius
		config.set("radius", origin.getBlockY());
		config.set("origin.x", origin.getBlockX());
		config.set("origin.y", 0);
		config.set("origin.z", origin.getBlockZ());
		
		//Loop through every block and save it's information to this temp config
		for(int i=0; i < blocks.size(); i++){
			String prefix = "blocks.a"+i+".";
			
			config.set(prefix+"x", blocks.get(i).x);
			config.set(prefix+"y", blocks.get(i).y);
			config.set(prefix+"z", blocks.get(i).z);
			
			config.set(prefix+"id", blocks.get(i).id);
			config.set(prefix+"data", blocks.get(i).data);
		}
		
		//Try to save the structure template		
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This loads a structure template as a YAML config and contructs an ArrayList
	 * @param name is the name of a structure, where the location is defined by main config entry
	 * @return the list of blocks for the 'name' structure
	 */
	public ArrayList<Block> getStructureBlockMeta(String name){
		//Debug info
		plugin.getLogger().info("Loading "+name);
		
		//Create a file and config for this structure
		File configFile;
		FileConfiguration config;
		
		//Construct a filepath based on the main config entry for 'name'
		String filepath = prefix + plugin.getConfig().getString("settings.structures."+name);
		
		//Init configFile and config so that we can access them
		configFile = new File(plugin.getDataFolder(), filepath);
		config = YamlConfiguration.loadConfiguration(configFile);
		
		//Grab the origin offset information from the structure
		int originX = config.getInt("origin.x");
		int originZ = config.getInt("origin.z");
		int originY = config.getInt("origin.y");
		
		//Create and init a list of blocks
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		//Loop through every block under the blocks kay
		for(String key : config.getConfigurationSection("blocks").getKeys(false)){
			
			//Define a prefix for the exact block location
			key = "blocks."+key;
			
			//Create and init a new block based on its saved location and the offset origin
			Block block = new Block();			
			block.x = config.getInt(key+".x") - originX;
			block.y = config.getInt(key+".y") - originY;
			block.z = config.getInt(key+".z") - originZ;
			block.id = config.getInt(key+".id");
			block.data = (byte)config.getInt(key+".data");
			
			//Add this new block to the blocks list
			blocks.add(block);
		}
		
		return blocks;
	}

	/**
	 * Structure class to define structure metadata and methods to store as and retrieve from Strings
	 * @author Conrad
	 *
	 */
	public class Structure{
		public int x_size,z_size;
		public int x_loc, y_loc, z_loc;
		public int radius;
		public String type;
		
		public String toString(){
		    return x_loc + "," + y_loc + "," + z_loc + "," + x_size + "," + z_size + "," + radius + "," + type;
		}
		
		public void setFromString(String s){
			String[] str = s.split(","); //split s by ','
			
			x_loc = Integer.parseInt(str[0]);
			y_loc = Integer.parseInt(str[1]);
			z_loc = Integer.parseInt(str[2]);
			x_size = Integer.parseInt(str[3]);
			z_size = Integer.parseInt(str[4]);
			radius = Integer.parseInt(str[5]);
			type = str[6]; //Get type
		}
		
	}
	
	/**
	 * Simple block class to allow for ArrayLists of blocks
	 * @author Conrad
	 *
	 */
	public class Block{
		public int id;
		public byte data;
		public int x,y,z;
	}
}
