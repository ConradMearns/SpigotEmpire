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
	
	EmpirePlugin plugin;
	
	private String prefix;
	private File structuresFile;
	public FileConfiguration structures;
	
	public BuildingManager(EmpirePlugin plugin){
		this.plugin = plugin;
		
		prefix = plugin.getConfig().getString("settings.structures.prefix");
		
		//Init configuration
		structuresFile = new File(plugin.getDataFolder(), "structures.yml");
		structures = YamlConfiguration.loadConfiguration(structuresFile);
		
	}
	
	public ArrayList<Block> getBuildingArray(String name){
		
		plugin.getLogger().info("Loading "+name);
		
		File configFile;
		FileConfiguration config;
		
		String filepath = prefix + plugin.getConfig().getString("settings.structures."+name);
		
		configFile = new File(plugin.getDataFolder(), filepath);
		config = YamlConfiguration.loadConfiguration(configFile);
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int originX = config.getInt("origin.x");
		int originZ = config.getInt("origin.z");
		int originY = config.getInt("origin.y");
		//int radius = config.getInt("radius");
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		for(String key : config.getConfigurationSection("blocks").getKeys(false)){
			
			//plugin.getLogger().info(key);
			
			key = "blocks."+key;
			
			Block block = new Block();			
			block.x = config.getInt(key+".x") - originX;
			block.y = config.getInt(key+".y") - originY;
			block.z = config.getInt(key+".z") - originZ;
			block.id = config.getInt(key+".id");
			block.data = (byte)config.getInt(key+".data");
			
			blocks.add(block);
		}
		
		return blocks;
	}
	
	public Structure getStructureInfo(String name){
		
		String filepath = prefix + plugin.getConfig().getString("settings.structures."+name);
		
		File configFile;
		FileConfiguration config;
		
		configFile = new File(plugin.getDataFolder(), filepath);
		config = YamlConfiguration.loadConfiguration(configFile);
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Structure struct = new Structure();
		
		struct.x_size = config.getInt("origin.x");
		struct.z_size = config.getInt("origin.z");
		struct.radius = config.getInt("radius");
		
		return struct;
	}
	
	//Build
	public void build(Player p, String name, Location loc){
		this.build(p, name, loc, 0);
	}
	
	public void build(Player p, String name, Location loc, int speed){
		this.build(p, name, loc, speed, false);
	}
	public void build(Player p, String name, Location loc, int speed, boolean sound){
		
		ArrayList<Block> blocks = getBuildingArray(name);
		Structure struct = getStructureInfo(name);
		
		Collections.shuffle(blocks);
		
		plugin.getLogger().info("Built structure at "+blocks.get(0).x + ", " +blocks.get(0).y + ", "+blocks.get(0).z + ", ");
		
		int index = 0;
		for(Block b : blocks){
			index++;
			new BukkitRunnable() {
		        
	            @Override
	            public void run() {
	                placeBlock(b.id, b.data, loc.getBlockX()+b.x, loc.getBlockY()+b.y, loc.getBlockZ()+b.z);
	                if(sound){
	                	plugin.world.playSound(loc, Sound.BLOCK_STONE_PLACE, 1F, 1F);
	                }
	            }
	            
	        }.runTaskLater(this.plugin, speed*index);
			
		}
		
		//The building was built!
		struct.type = name;
		struct.x_loc = loc.getBlockX();
		struct.z_loc = loc.getBlockZ();
		struct.y_loc = loc.getBlockY();
		saveStructureConfig(struct, p);
		
	}
	
	public void saveStructureConfig(Structure struct, Player p){
		//Grab list of all buildings
		@SuppressWarnings("unchecked")
		ArrayList<String> buildings = (ArrayList<String>) structures.getList(p.getDisplayName());
		
		if(buildings == null){
			buildings = new ArrayList<String>();
		}
		
		buildings.add(struct.toString());
		structures.set(p.getDisplayName(), buildings);
		try {
			structures.save(structuresFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Warning! Y-value in location is used for radius!
	public void saveToFile(ArrayList<Block> blocks, String name, Location location){
		
		String filepath = prefix + plugin.getConfig().getString("settings.structures."+name);
		
		plugin.getLogger().info("Saving "+blocks.size()+" block to "+filepath);
		
		File configFile;
		FileConfiguration config;
		
		configFile = new File(plugin.getDataFolder(), filepath);
		config = YamlConfiguration.loadConfiguration(configFile);
		
		//Save a default value for an origin offset and radius
		config.set("radius", location.getBlockY());
		config.set("origin.x", location.getBlockX());
		config.set("origin.y", 0);
		config.set("origin.z", location.getBlockZ());
		
		for(int i=0; i < blocks.size(); i++){
			String prefix = "blocks.a"+i+".";
			
			config.set(prefix+"x", blocks.get(i).x);
			config.set(prefix+"y", blocks.get(i).y);
			config.set(prefix+"z", blocks.get(i).z);
			
			config.set(prefix+"id", blocks.get(i).id);
			config.set(prefix+"data", blocks.get(i).data);
		}
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Structure> getEmpireStructureList(String empire){
		ArrayList<Structure> structureList = new ArrayList<Structure>();
		
		@SuppressWarnings("unchecked")
		ArrayList<String> stringList = (ArrayList<String>) plugin.buildingManager.structures.get(empire);
		for(String string : stringList){
			Structure structure = new Structure();
			structure.setFromString(string);
			structureList.add(structure);
			
		}
		
		return structureList;
	}
	
	@SuppressWarnings("deprecation")
	public void saveStructureBetween(Location pos1, Location pos2, String name) {
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
		saveToFile(blocks, name, offsetRadius);
	}

	@SuppressWarnings("deprecation")
	public void placeBlock(int id, byte data, int x, int y, int z){
		plugin.world.getBlockAt(x, y, z).setTypeId(id);
		plugin.world.getBlockAt(x, y, z).setData(data);
	}
	
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
	
	public class Block{
		public int id;
		public byte data;
		public int x,y,z;
	}
}
