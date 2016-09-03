package com.estrayer.empire;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class BuildingManager {
	
	public static String HOUSE;
	
	EmpirePlugin plugin;
	
	public BuildingManager(EmpirePlugin plugin){
		this.plugin = plugin;
		
		String prefix = plugin.getConfig().getString("settings.structures.prefix");
		
		BuildingManager.HOUSE = prefix + plugin.getConfig().getString("settings.structures.house");
	}
	
	public ArrayList<Block> getArrayFromFile(String filepath){
		
		plugin.getLogger().info("Loading from "+filepath);
		
		File configFile;
		FileConfiguration config;
		
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
	
	public void buildInstantly(String filepath, Location loc){
		ArrayList<Block> blocks = getArrayFromFile(filepath);
		
		plugin.getLogger().info("Built structure at "+blocks.get(0).x + ", " +blocks.get(0).y + ", "+blocks.get(0).z + ", ");
		
		for(Block b : blocks){
			placeBlock(b.id, b.data, loc.getBlockX()+b.x, loc.getBlockY()+b.y, loc.getBlockZ()+b.z);
		}
	}
	
	public void buildOverTime(String filepath, Location loc, int speed){
		
		ArrayList<Block> blocks = getArrayFromFile(filepath);
		
		Collections.shuffle(blocks);
		
		plugin.getLogger().info("Built structure at "+blocks.get(0).x + ", " +blocks.get(0).y + ", "+blocks.get(0).z + ", ");
		
		int index = 0;
		for(Block b : blocks){
			index++;
			new BukkitRunnable() {
		        
	            @Override
	            public void run() {
	                placeBlock(b.id, b.data, loc.getBlockX()+b.x, loc.getBlockY()+b.y, loc.getBlockZ()+b.z);
	                plugin.world.playSound(loc, Sound.BLOCK_STONE_PLACE, 1F, 1F);
	            }
	            
	        }.runTaskLater(this.plugin, speed*index);
			
		}
		
	}
	
	//Warning! Y-value in location is used for radius!
	public void saveToFile(ArrayList<Block> blocks, String filepath, Location location){
		
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

	@SuppressWarnings("deprecation")
	public void saveStructureBetween(Location pos1, Location pos2, String fileLocation) {
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
		saveToFile(blocks, fileLocation, offsetRadius);
	}

	@SuppressWarnings("deprecation")
	public void placeBlock(int id, byte data, int x, int y, int z){
		plugin.world.getBlockAt(x, y, z).setTypeId(id);
		plugin.world.getBlockAt(x, y, z).setData(data);
	}
	
	public class Block{
		public int id;
		public byte data;
		public int x,y,z;
	}
}
