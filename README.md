# Project Goals - To Determine
## Minecraft Empire RT-Strategy Plugin

### General Objective
Develop a server-plugin for Minecraft that turns gameplay into a real-time strategy empire building game for multiple players over long durations.

### Systems and Technology
Using either Spigot or Bukkit as a backend, develop a plugin using pre-determined API structures in Java. Keep code maintained with Git, so that multiple members can work together.

### Required Features
* Adventure-Mode type land protection
* Administrative / dev mode for testing
* New player auto-empire generation
* Alliance and enemy options
* Grid based building and world modification
  * Includes player-structures and natural structures
* More predictable terrain generation to prevent ugly worlds
* Basic new mob AI
  * Villagers to preform mining, farming, building, etc
  * Skeletons and other mobs as militia, archers, etc
* Trading and trade routes

##### FPS-Specific Features
* Building interaction based commands and actions
  * Resource management as a conflict of space and delivery
  * Villager assignment based on required tasks and quotas
  * Combat unit functions and controls managed by Combat Leaders
    * Control Combat Leader by playing as the selected unit, interchangeable on death or by choice

##### Spectator-Specific Features
* Menu based commands and actions
  * Resource management controlled in menu or stats
  * Villager assignment and actions given via menu
  * Combat unit functions and controls given via menu
  * Fog of War / peek protection

##### Cosmetic Features
* Grass degrades into path when walked on
* Resource delivery with mule/cart
* Alternate building styles

### Feature Implementation
* ~~Asynchronous block placement~~
* Structures
  * Structure files test placement
  * ~~Structure files define boundaries~~
  * ~~Individual structure origins are stored in empire-specific file~~
  * ~~Stop saving air~~
  * ~~Structures have origin offset and a radius defined in the file~~
  * ~~Load blocks and data from yml~~
  * ~~Save structures in id:data lists yml~~
* New player auto-empire
  * Generate starting buildings
  * ~~Reference list of all empire-players~~
  * ~~Empire names are dependent on player name~~
  * ~~Save empire data to yml~~
  * ~~Teleport player to new location~~
* ~~Toggle-able dev mode~~
* ~~Ensure adventure mode for all joining players~~
