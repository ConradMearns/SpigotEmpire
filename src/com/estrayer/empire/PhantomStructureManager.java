package com.estrayer.empire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.estrayer.empire.BuildingManager.Structure;

public class PhantomStructureManager {

	EmpirePlugin plugin;
	HashMap<String, ArrayList<PhantomBlock>> phantoms;
	
	/**
	 * Constructor, init list of phantoms
	 * @param plugin
	 */
	public PhantomStructureManager(EmpirePlugin plugin){
		this.plugin = plugin;
		phantoms = new HashMap<String, ArrayList<PhantomBlock>>();
	}
	
	/**
	 * Update every phantom block per player
	 * @param player
	 * @param structType
	 */
	public void updatePhantoms(Player player, String structType){
		if(phantoms.containsKey(player.getDisplayName())){
			Location origin = player.getTargetBlock((Set<Material>)null, 30).getLocation();
			
			Structure proposedStruct = plugin.buildingManager.getStructureMeta(structType, origin);
			
			boolean perm = plugin.buildingManager.permissionToBuild(proposedStruct);
			
			for(PhantomBlock phantom : phantoms.get(player.getDisplayName())){
				phantom.update(origin);
				if(perm){
					phantom.setGood();
				}else{
					phantom.setBad();
				}
			}
		}else{
			createPhantomStructure(player, structType);
		}
		

	}
	
	/**
	 * Remove phantoms specific to player
	 * @param player
	 */
	public void deletePhantoms(Player player){
		if(phantoms.containsKey(player.getDisplayName())){
			for(PhantomBlock phantom : phantoms.get(player.getDisplayName())){
				phantom.remove();
			}
			phantoms.remove(player.getDisplayName());
		}
		
	}
	
	/**
	 * Generate phantoms in the shape of a structure
	 * @param player
	 * @param structName
	 */
	public void createPhantomStructure(Player player, String structName){
		
		Location origin = player.getTargetBlock((Set<Material>)null, 30).getLocation();
		
		ArrayList<PhantomBlock> al = new ArrayList<PhantomBlock>();
		
		Structure struct = plugin.buildingManager.getStructureMeta(structName);
		
		for(int x=(-1*(struct.radius-1)); x < struct.radius; x++){
			for(int z=(-1*(struct.radius-1)); z < struct.radius; z++){
				PhantomBlock pBlock = new PhantomBlock();
				pBlock.create(origin);
				pBlock.x = x;
				pBlock.z = z;
				al.add(pBlock);
			}
		}
		

		
		
		phantoms.put(player.getDisplayName(), al);
	}
	
	/**
	 * ArmorStand that is based around the local Block class
	 * @author Conrad
	 */
	private class PhantomBlock extends Block{
		
		ArmorStand phantom;
		
		public void setGood(){
			ItemStack helmet = new ItemStack(Material.STAINED_GLASS, 1, (short) 13);
			
			phantom.setHelmet(helmet);
		}
		
		public void setBad(){
			ItemStack helmet = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
			
			phantom.setHelmet(helmet);
		}
		
		public void create(Location location){
			phantom = (ArmorStand)plugin.world.spawn(location, ArmorStand.class);
			phantom.setGravity(false);
			
			ItemStack helmet = new ItemStack(Material.STAINED_GLASS, 1, (short) 13);
			
			phantom.setHelmet(helmet);
			phantom.setCollidable(false);
			phantom.setVisible(false);
		}
		
		public void update(Location origin){
			Location location = phantom.getLocation();
			location.setX(origin.getX() + x + 0.5);
			location.setY(origin.getY() + y - 0.5);
			location.setZ(origin.getZ() + z + 0.5);
			
			phantom.teleport(location);
		}
		
		public void remove(){
			phantom.remove();
		}
		
	}
	
}
