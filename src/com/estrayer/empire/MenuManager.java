package com.estrayer.empire;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuManager implements Listener{
	
	EmpirePlugin plugin;
	
	/**
	 * Global inventory to show a list of buildings we can build
	 */
	public Inventory buildMenu;
	
	/**
	 * Construct class and init inventory menus
	 * @param plugin
	 */
	public MenuManager(EmpirePlugin plugin){
		this.plugin = plugin;
		
		//Init buildMenu
		buildMenu = Bukkit.createInventory(null, 9, "Build Menu");
		initBuildMenu();
	}
	
	/**
	 * Initialize a player's inventory when they join the server for the first time
	 * @param player
	 */
	public void initPlayerInventory(Player player){
		ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
		
		//Fill inv
		for(int i=9; i<=35; i++){
			player.getInventory().setItem(i, glass);
		}
		
		ItemStack buildTool = new ItemStack(Material.WOOD_AXE);
		ItemMeta im = buildTool.getItemMeta();
		im.setDisplayName("Build");
		buildTool.setItemMeta(im);
		
		player.getInventory().addItem(buildTool);
		
		ItemStack res = new ItemStack(Material.LOG);
		im = res.getItemMeta();
		im.setDisplayName("Wood - 0");
		res.setItemMeta(im);
		
		player.getInventory().setItem(4, res);
		
		res = new ItemStack(Material.PORK);
		im = res.getItemMeta();
		im.setDisplayName("Food - 0");
		res.setItemMeta(im);
		
		player.getInventory().setItem(5, res);
		
		res = new ItemStack(Material.COBBLESTONE);
		im = res.getItemMeta();
		im.setDisplayName("Stone - 0");
		res.setItemMeta(im);
		
		player.getInventory().setItem(6, res);

		res = new ItemStack(Material.IRON_INGOT);
		im = res.getItemMeta();
		im.setDisplayName("Iron - 0");
		res.setItemMeta(im);
		
		player.getInventory().setItem(7, res);
		
		res = new ItemStack(Material.GOLD_INGOT);
		im = res.getItemMeta();
		im.setDisplayName("Gold - 0");
		res.setItemMeta(im);
		
		player.getInventory().setItem(8, res);
	}
	
	/**
	 * Update the player inventory to reflect changes in resources
	 * @param player
	 */
	public void updatePlayerInventory(Player player){
		Inventory inv = player.getInventory();
		
		ItemStack wood = inv.getItem(4);
		ItemStack food = inv.getItem(5);
		ItemStack stone = inv.getItem(6);
		ItemStack iron = inv.getItem(7);
		ItemStack gold = inv.getItem(8);
		
		ItemMeta im = wood.getItemMeta();
		String name = im.getDisplayName();
		name = name.split(" -")[0];
		name = name+" - "+plugin.getPlayerResourceAmount(player.getDisplayName(), "wood");
		im.setDisplayName(name);
		wood.setItemMeta(im);
		
		im = food.getItemMeta();
		name = im.getDisplayName();
		name = name.split(" -")[0];
		name = name+" - "+plugin.getPlayerResourceAmount(player.getDisplayName(), "food");
		im.setDisplayName(name);
		food.setItemMeta(im);
		
		im = stone.getItemMeta();
		name = im.getDisplayName();
		name = name.split(" -")[0];
		name = name+" - "+plugin.getPlayerResourceAmount(player.getDisplayName(), "stone");
		im.setDisplayName(name);
		stone.setItemMeta(im);
		
		im = iron.getItemMeta();
		name = im.getDisplayName();
		name = name.split(" -")[0];
		name = name+" - "+plugin.getPlayerResourceAmount(player.getDisplayName(), "iron");
		im.setDisplayName(name);
		iron.setItemMeta(im);
		
		im = gold.getItemMeta();
		name = im.getDisplayName();
		name = name.split(" -")[0];
		name = name+" - "+plugin.getPlayerResourceAmount(player.getDisplayName(), "gold");
		im.setDisplayName(name);
		gold.setItemMeta(im);
	}
	
	/**
	 * Listen to inventory clicks, handle inventory menu clicks and player inventory clicks
	 * @param event
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		
		ItemStack clicked = event.getCurrentItem();
		
		Inventory inventory = event.getInventory();
		
		//If we clicked on an item inside the build menu, do stuff
		if (inventory.getName().equals(buildMenu.getName())) {
			if(clicked.hasItemMeta()){
				if (clicked.getItemMeta().getDisplayName().equals("House")){
					plugin.buildingManager.requestBuild(player, "house");
				}
				if (clicked.getItemMeta().getDisplayName().equals("Farm")){
					plugin.buildingManager.requestBuild(player, "farm");
				}
				if (clicked.getItemMeta().getDisplayName().equals("Town Center")){
					plugin.buildingManager.requestBuild(player, "town_center");
				}
				event.setCancelled(true);
				player.closeInventory();
			}
		
		}
		
		//Stop player from manipulating own inventory
		if (inventory.getViewers().contains(player) && player.getGameMode().equals(GameMode.ADVENTURE)){
			event.setCancelled(true);
		}
	}
	
	/**
	 * Initialize the buildMenu with items and their costs
	 */
	private void initBuildMenu(){
		ItemStack item = new ItemStack(Material.WOOD_DOOR);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName("House");
		im.setLore(plugin.buildingManager.getRequirementList("house"));
		item.setItemMeta(im);
		
		buildMenu.setItem(0, item);
		
		item = new ItemStack(Material.WHEAT);
		im = item.getItemMeta();
		im.setDisplayName("Farm");
		im.setLore(plugin.buildingManager.getRequirementList("farm"));
		item.setItemMeta(im);
		
		buildMenu.setItem(1, item);
		
		item = new ItemStack(Material.JUKEBOX);
		im = item.getItemMeta();
		im.setDisplayName("Town Center");
		im.setLore(plugin.buildingManager.getRequirementList("town_center"));
		item.setItemMeta(im);
		
		buildMenu.setItem(2, item);
	}
	
}
