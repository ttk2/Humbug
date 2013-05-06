Humbug
======

Minecraft server plug-in: Simply toggles various functionality

- Disables Anvil use.
- Disables Ender Chest use.
- Disables Villager trading.
- Disables Portal creation.
- Disables Ender dragon.
- Disables join/quit/kick announcements.
- Enables personal death message with coords
- Disables Death announcements.
- Enables Red coloring on Death announcements.
- Enables death message logging.
- Disables Enderman Griefing (stealing blocks)
- Enables Unlimited Cauldron Water
- Enables Quartz from Gravel
- Disables Wither block destruction radius.
- Disables Wither and Wither Skull explosions.
- Disables cobblestone generation from lava and water.
- Makes the Protection enchantment a little stronger
- Enables adjustment of all Player maximum health
- Fixes a rail duplication bug.
- Fixes a teleport bug when players log out while riding vehicles.
- Removes the Enchanted Book recipe.
- Removes the Enchanted Golden Apple recipe.
- Converts Enchanted Golden Apples to normal Golden Apples if a Player attempts to eat them.
- Disables Ender Pearl Teleportation
- Alters the drop rate of wither skulls
- Removes specific items from dropping when a mob is killed
- Prevents records from playing in jukeboxes
- Disabled dying sheep
- Disables water usage in main world hell biomes

The 'humbug' console command can be used to get or set any of the configuration file settings while the server is running. Also available are 'humbug save' and 'humbug reload'.

Config file settings:
- debug: Boolean, Turns on debug logging (currently there is none)
- anvil: Boolean, Turns on anvil use
- ender_chest: Boolean, Turns on ender chest use
- ender_chests_placeable: Boolean, Allows ender chests to be placed
- villager_trades: Boolean, Turns on villager trades
- portalcreate: Boolean, turns on portal creation
- enderdragon: Boolean, turns on ender dragon
- joinquitkick: Boolean, show part/quit and kick announcements
- deathpersonal: Boolean, send a private message to the person who dies, with coords
- deathannounce: Boolean, death message announcements
- deathred: Boolean, makes death announce messages red
- deathlog: Boolean, logs player deaths to the console
- endergrief: Boolean, turns on enderman stealing blocks
- unlimitedcauldron: Boolean, turns on unlimited cauldron water
- quartz_gravel_percentage: Integer between 0 and 100, 0 is standard behavior.
- wither: Boolean, Turns on the wither
- wither_explosions: Boolean, Turns on wither explosions destroying blocks. Wither/Wither Skull explosions will always occur to damage players, this only effects block breakage.
- wither_insta_break: Boolean, Turns on the wither insta-break ability
- cobble_from_lava: Boolean, Turns on cobblestone generation when lava and water mix
- cobble_from_lava_scan_radius: Integer, Sets the radius to scan for lava/water interaction
- ench_book_craftable: Boolean, Allows the Enchanted Book recipe to be used
- scale_protection_enchant: Boolean, Increases damage reduction granted by the Protection enchantment
- fix_rail_dup_bug: Boolean, Fixes a rail duplication bug
- fix_vehicle_logout_bug: Boolean, Fixes a teleport bug when players logout in vehicles
- player_max_health: Integer, sets all Player maximum health
- ender_pearl_teleportation: Boolean, Turns on Ender Pearl teleportation
- ench_gold_app_edible: Boolean, Allows players to eat Enchanted Golden Apples. If false, Enchanted Golden Apples are converted to normal Golden Apples
- ench_gold_app_craftable: Boolean, Allows the Enchanted Golden Apple recipe to be used
- wither_skull_drop_rate: Integer between -1 and 1000000, -1 is standard behavior. If a random number [0, 1000000) is less then this value, a wither skull drops. For example, 200000 is a 20% drop rate.
- remove_mob_drops: List<Integer>, a list of item IDs to remove from the dropped items when mobs are killed. The in-game command can also accept Material names.
- disallow_record_playing: Boolean, Disables the ability for records to play in jukeboxes
- allow_dye_sheep: Boolean, defaults to true. Allows sheep to be dyed directly (rather than dyeing each bit of wool).
- allow_water_in_nether: Boolean, enables water use in hell biomes

Default configuration (biased for CivCraft):
- debug: false
- anvil: false
- ender_chest: false
- ender_chests_placeable: true
- villager_trades: false
- portalcreate: true
- enderdragon: true
- joinquitkick: true
- deathpersonal: false
- deathannounce: true
- deathred: false
- deathlog: false
- endergrief: true
- unlimitedcauldron: false
- quartz_gravel_percentage: 0
- wither: true
- wither_explosions: false
- wither_insta_break: false
- cobble_from_lava: false
- cobble_from_lava_scan_radius: 0
- ench_book_craftable: false
- scale_protection_enchant: true
- fix_rail_dup_bug: true
- fix_vehicle_logout_bug: true
- player_max_health: 20
- ender_pearl_teleportation: true
- ench_gold_app_edible: false
- ench_gold_app_craftable: false
- wither_skull_drop_rate: -1
- remove_mob_drops:
- disallow_record_playing: true
- allow_dye_sheep: true
- allow_water_in_nether: false
