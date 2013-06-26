package com.untamedears.humbug;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Humbug extends JavaPlugin implements Listener {
  public static void severe(String message) {
    log_.severe("[Humbug] " + message);
  }

  public static void warning(String message) {
    log_.warning("[Humbug] " + message);
  }

  public static void info(String message) {
    log_.info("[Humbug] " + message);
  }

  public static void debug(String message) {
    if (config_.getDebug()) {
      log_.info("[Humbug] " + message);
    }
  }

  public static Humbug getPlugin() {
    return global_instance_;
  }

  private static final Logger log_ = Logger.getLogger("Humbug");
  private static Humbug global_instance_ = null;
  private static Config config_ = null;
  private static int max_golden_apple_stack_ = 1;

  static {
    max_golden_apple_stack_ = Material.GOLDEN_APPLE.getMaxStackSize();
    if (max_golden_apple_stack_ > 64) {
      max_golden_apple_stack_ = 64;
    }
  }

  private Random prng_ = new Random();

  public Humbug() {}

  // ================================================
  // Reduce registered PlayerInteractEvent count. onPlayerInteractAll handles
  //  cancelled events.

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    onAnvilOrEnderChestUse(event);
    if (!event.isCancelled()) {
      onCauldronInteract(event);
    }
    if (!event.isCancelled()) {
      onRecordInJukebox(event);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST) // ignoreCancelled=false
  public void onPlayerInteractAll(PlayerInteractEvent event) {
    onPlayerEatGoldenApple(event);
    onPlayerPearlTeleport(event);
  }

  // ================================================
  // Stops people from dying sheep

  @EventHandler
  public void onDyeWool(SheepDyeWoolEvent event) {
    if (!config_.getAllowDyeSheep()) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // Fixes Teleporting through walls and doors
  // ** and **
  // Ender Pearl Teleportation disabling

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    TeleportCause cause = event.getCause();
    if (cause != TeleportCause.ENDER_PEARL) {
        return;
    } else if (!config_.getEnderPearlTeleportationEnabled()) {
      event.setCancelled(true);
      return;
    }
    if (!config_.getTeleportFixEnabled()) {
      return;
    }
    Location to = event.getTo();
    World world = to.getWorld();

    // From and To are feet positions.  Check and make sure we can teleport to a location with air
    // above the To location.
    Block toBlock = world.getBlockAt(to);
    Block aboveBlock = world.getBlockAt(to.getBlockX(), to.getBlockY()+1, to.getBlockZ());
    Block belowBlock = world.getBlockAt(to.getBlockX(), to.getBlockY()-1, to.getBlockZ());
    boolean lowerBlockBypass = false;
    double height = 0.0;
    switch( toBlock.getType() ) {
    case CHEST: // Probably never will get hit directly
    case ENDER_CHEST: // Probably never will get hit directly
      height = 0.875;
      break;
    case STEP:
      lowerBlockBypass = true;
      height = 0.5;
      break;
    case WATER_LILY:
      height = 0.016;
      break;
    case ENCHANTMENT_TABLE:
      lowerBlockBypass = true;
      height = 0.75;
      break;
    case BED:
    case BED_BLOCK:
      // This one is tricky, since even with a height offset of 2.5, it still glitches.
      //lowerBlockBypass = true;
      //height = 0.563;
      // Disabling teleporting on top of beds for now by leaving lowerBlockBypass false.
      break;
    case FLOWER_POT:
    case FLOWER_POT_ITEM:
      height = 0.375;
      break;
    case SKULL: // Probably never will get hit directly
      height = 0.5;
      break;
    default:
      break;
    }
    // Check if the below block is difficult
    // This is added because if you face downward directly on a gate, it will
    // teleport your feet INTO the gate, thus bypassing the gate until you leave that block.
    switch( belowBlock.getType() ) {
    case FENCE:
    case FENCE_GATE:
    case NETHER_FENCE:
    case COBBLE_WALL:
      height = 0.5;
      break;
    default:
      break;
    }

    boolean upperBlockBypass = false;
    if( height >= 0.5 ) {
      Block aboveHeadBlock = world.getBlockAt(aboveBlock.getX(), aboveBlock.getY()+1, aboveBlock.getZ());
      if( false == aboveHeadBlock.getType().isSolid() ) {
        height = 0.5;
      } else {
        upperBlockBypass = true; // Cancel this event.  What's happening is the user is going to get stuck due to the height.
      }
    }

    // Normalize teleport to the center of the block.  Feet ON the ground, plz.
    // Leave Yaw and Pitch alone
    to.setX(Math.floor(to.getX()) + 0.5000);
    to.setY(Math.floor(to.getY()) + height);
    to.setZ(Math.floor(to.getZ()) + 0.5000);

    if(aboveBlock.getType().isSolid() ||
       (toBlock.getType().isSolid() && !lowerBlockBypass) ||
       upperBlockBypass ) {
      // One last check because I care about Top Nether.  (someone build me a shrine up there)
      boolean bypass = false;
      if ((world.getEnvironment() == Environment.NETHER) &&
          (to.getBlockY() > 124) && (to.getBlockY() < 129)) {
        bypass = true;
      }
      if (!bypass) {
        event.setCancelled(true);
      }
    }
  }

  // ================================================
  // Villager Trading

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (config_.getVillagerTradesEnabled()) {
      return;
    }
    Entity npc = event.getRightClicked();
    if (npc == null) {
        return;
    }
    if (npc.getType() == EntityType.VILLAGER) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // Anvil and Ender Chest usage

  // EventHandler registered in onPlayerInteract
  public void onAnvilOrEnderChestUse(PlayerInteractEvent event) {
    if (config_.getAnvilEnabled() &&
        config_.getEnderChestEnabled()) {
      return;
    }
    Action action = event.getAction();
    Material material = event.getClickedBlock().getType();
    boolean anvil = !config_.getAnvilEnabled() &&
                    action == Action.RIGHT_CLICK_BLOCK &&
                    material.equals(Material.ANVIL);
    boolean ender_chest = !config_.getEnderChestEnabled() &&
                          action == Action.RIGHT_CLICK_BLOCK &&
                          material.equals(Material.ENDER_CHEST);
    if (anvil || ender_chest) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled=true)
  public void onEnderChestPlace(BlockPlaceEvent e) {
    Material material = e.getBlock().getType();
    if (!config_.getEnderChestsPlaceable() && material == Material.ENDER_CHEST) {
      e.setCancelled(true);
    }
  }

  // ================================================
  // Unlimited Cauldron water

  // EventHandler registered in onPlayerInteract
  public void onCauldronInteract(PlayerInteractEvent e) {
    if (!config_.getUnlimitedCauldronEnabled()) {
      return;
    }

    // block water going down on cauldrons
    if(e.getClickedBlock().getType() == Material.CAULDRON && e.getMaterial() == Material.GLASS_BOTTLE && e.getAction() == Action.RIGHT_CLICK_BLOCK)
    {
      Block block = e.getClickedBlock();
      if(block.getData() > 0)
      {
        block.setData((byte)(block.getData()+1));
      }
    }
  }

  // ================================================
  // Quartz from Gravel

  @EventHandler(ignoreCancelled=true, priority = EventPriority.HIGHEST)
  public void onGravelBreak(BlockBreakEvent e) {
    if(e.getBlock().getType() != Material.GRAVEL || config_.getQuartzGravelPercentage() == 0) {
      return;
    }

    if(prng_.nextInt(100) < config_.getQuartzGravelPercentage())
    {
      e.setCancelled(true);
      e.getBlock().setType(Material.AIR);
      e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(Material.QUARTZ, 1));
    }
  }

  // ================================================
  // Portals

  @EventHandler(ignoreCancelled=true)
  public void onPortalCreate(PortalCreateEvent e) {
    if (!config_.getPortalCreateEnabled()) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled=true)
  public void onEntityPortalCreate(EntityCreatePortalEvent e) {
    if (!config_.getPortalCreateEnabled()) {
      e.setCancelled(true);
    }
  }

  // ================================================
  // EnderDragon

  @EventHandler(ignoreCancelled=true)
  public void onDragonSpawn(CreatureSpawnEvent e) {
    if (e.getEntityType() == EntityType.ENDER_DRAGON && !config_.getEnderDragonEnabled()) {
      e.setCancelled(true);
    }
  }

  // ================================================
  // Join/Quit/Kick messages

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent e) {
    if (!config_.getJoinQuitKickEnabled()) {
      e.setJoinMessage(null);
    }
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onQuit(PlayerQuitEvent e) {
    if (!config_.getJoinQuitKickEnabled()) {
      e.setQuitMessage(null);
    }
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onKick(PlayerKickEvent e) {
    if (!config_.getJoinQuitKickEnabled()) {
      e.setLeaveMessage(null);
    }
  }

  // ================================================
  // Death Messages

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onDeath(PlayerDeathEvent e) {
    boolean log_msg = config_.getDeathLoggingEnabled();
    boolean send_personal = config_.getDeathMessagePersonalEnabled();
    if (log_msg || send_personal) {
      Location location = e.getEntity().getLocation();
      String msg = String.format(
          "%s ([%s] %d, %d, %d)", e.getDeathMessage(), location.getWorld().getName(),
          location.getBlockX(), location.getBlockY(), location.getBlockZ());
      if (log_msg) {
        info(msg);
      }
      if (send_personal) {
        e.getEntity().sendMessage(ChatColor.RED + msg);
      }
    }
    if (!config_.getDeathAnnounceEnabled()) {
      e.setDeathMessage(null);
    } else if (config_.getDeathMessageRedEnabled()) {
      e.setDeathMessage(ChatColor.RED + e.getDeathMessage());
    }
  }

  // ================================================
  // Endermen Griefing
  @EventHandler(ignoreCancelled=true)
  public void onEndermanGrief(EntityChangeBlockEvent e)
  {
    if (!config_.getEndermenGriefEnabled() && e.getEntity() instanceof Enderman) {
      e.setCancelled(true);
    }
  }

  // ================================================
  // Wither Insta-breaking and Explosions

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (config_.getWitherInstaBreakEnabled()) {
      return;
    }
    Entity npc = event.getEntity();
    if (npc == null) {
        return;
    }
    EntityType npc_type = npc.getType();
    if (npc_type.equals(EntityType.WITHER)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    if (config_.getWitherExplosionsEnabled()) {
      return;
    }
    Entity npc = event.getEntity();
    if (npc == null) {
        return;
    }
    EntityType npc_type = npc.getType();
    if ((npc_type.equals(EntityType.WITHER) ||
         npc_type.equals(EntityType.WITHER_SKULL))) {
      event.blockList().clear();
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onWitherSpawn(CreatureSpawnEvent event) {
    if (config_.getWitherEnabled()) {
      return;
    }
    if (!event.getEntityType().equals(EntityType.WITHER)) {
      return;
    }
    event.setCancelled(true);
  }

  // ================================================
  // Prevent specified items from dropping off mobs

  public void removeItemDrops(EntityDeathEvent event) {
    if (!config_.doRemoveItemDrops()) {
      return;
    }
    if (event.getEntity() instanceof Player) {
      return;
    }
    Set<Integer> remove_ids = config_.getRemoveItemDrops();
    List<ItemStack> drops = event.getDrops();
    ItemStack item;
    int i = drops.size() - 1;
    while (i >= 0) {
      item = drops.get(i);
      if (remove_ids.contains(item.getTypeId())) {
        drops.remove(i);
      }
      --i;
    }
  }

  // ================================================
  // Spawn more Wither Skeletons and Ghasts

  @EventHandler(ignoreCancelled=true)
  public void spawnMoreHellMonsters(CreatureSpawnEvent e) {
    if (config_.getExtraWitherSkeleSpawnRate() <= 0
        && config_.getExtraGhastSpawnRate() <= 0) {
      return;
    }
    if (e.getEntityType() == EntityType.PIG_ZOMBIE) {
      if(prng_.nextInt(1000000) < config_.getExtraWitherSkeleSpawnRate()) {
        e.setCancelled(true);
        Location loc = e.getLocation();
        World world = loc.getWorld();
        world.spawnEntity(loc, EntityType.SKELETON);
      } else if(prng_.nextInt(1000000) < config_.getExtraGhastSpawnRate()) {
        e.setCancelled(true);
        Location loc = e.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        World world = loc.getWorld();
        List<Integer> heights = new ArrayList<Integer>(16);
        int lastBlockHeight = 2;
        int emptyCount = 0;
        int maxHeight = world.getHighestBlockYAt(x, z);
        for (int y = 2; y < maxHeight; ++y) {
          Block block = world.getBlockAt(x, y, z);
          if (block.isEmpty()) {
            ++emptyCount;
            if (emptyCount == 11) {
              heights.add(lastBlockHeight + 2);
            }
          } else {
            lastBlockHeight = y;
            emptyCount = 0;
          }
        }
        if (heights.size() <= 0) {
          return;
        }
        loc.setY(heights.get(prng_.nextInt(heights.size())));
        world.spawnEntity(loc, EntityType.GHAST);
      }
    } else if (e.getEntityType() == EntityType.SKELETON
        && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
      Entity entity = e.getEntity();
      if (entity instanceof Skeleton) {
        Skeleton skele = (Skeleton)entity;
        skele.setSkeletonType(SkeletonType.WITHER);
        EntityEquipment entity_equip = skele.getEquipment();
        entity_equip.setItemInHand(new ItemStack(Material.STONE_SWORD));
        entity_equip.setItemInHandDropChance(0.0F);
      }
    }
  }

  // ================================================
  // Wither Skull drop rate

  public static final int skull_id_ = Material.SKULL_ITEM.getId();
  public static final byte wither_skull_data_ = 1;

  public void adjustWitherSkulls(EntityDeathEvent event) {
    Entity entity = event.getEntity();
    if (!(entity instanceof Skeleton)) {
      return;
    }
    int rate = config_.getWitherSkullDropRate();
    if (rate < 0 || rate > 1000000) {
      return;
    }
    Skeleton skele = (Skeleton)entity;
    if (skele.getSkeletonType() != SkeletonType.WITHER) {
      return;
    }
    List<ItemStack> drops = event.getDrops();
    ItemStack item;
    int i = drops.size() - 1;
    while (i >= 0) {
      item = drops.get(i);
      if (item.getTypeId() == skull_id_
          && item.getData().getData() == wither_skull_data_) {
        drops.remove(i);
      }
      --i;
    }
    if (rate - prng_.nextInt(1000000) <= 0) {
      return;
    }
    item = new ItemStack(Material.SKULL_ITEM);
    item.setAmount(1);
    item.setDurability((short)wither_skull_data_);
    drops.add(item);
  }

  // ================================================
  // Generic mob drop rate adjustment

  public void adjustMobItemDrops(EntityDeathEvent event){
    Entity mob = event.getEntity();
    if (mob instanceof Player){
      return;
    }
    if (mob.getWorld() == Bukkit.getWorld("world_the_end")) {
      return;
    }
    // Try specific multiplier, if that doesn't exist use generic
    EntityType mob_type = mob.getType();
    int multiplier = config_.getLootMultiplier(mob_type.toString());
    if (multiplier == 1) {
      multiplier = config_.getLootMultiplier("generic");
    }
    for (ItemStack item : event.getDrops()) {
      int amount = item.getAmount() * multiplier;   
      item.setAmount(amount);
    }  
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityDeathEvent(EntityDeathEvent event) {
    removeItemDrops(event);
    adjustWitherSkulls(event);
    adjustMobItemDrops(event);
  }

  // ================================================
  // Enchanted Golden Apple

  public boolean isEnchantedGoldenApple(ItemStack item) {
    // Golden Apples are GOLDEN_APPLE with 0 durability
    // Enchanted Golden Apples are GOLDEN_APPLE with 1 durability
    if (item == null) {
      return false;
    }
    if (item.getDurability() != 1) {
      return false;
    }
    Material material = item.getType();
    return material.equals(Material.GOLDEN_APPLE);
  }

  public void replaceEnchantedGoldenApple(
      String player_name, ItemStack item, int inventory_max_stack_size) {
    if (!isEnchantedGoldenApple(item)) {
      return;
    }
    int stack_size = max_golden_apple_stack_;
    if (inventory_max_stack_size < max_golden_apple_stack_) {
      stack_size = inventory_max_stack_size;
    }
    info(String.format(
          "Replaced %d Enchanted with %d Normal Golden Apples for %s",
          item.getAmount(), stack_size, player_name));
    item.setDurability((short)0);
    item.setAmount(stack_size);
  }

  public void removeRecipies() {
    if (config_.getEnchGoldAppleCraftable()) {
      return;
    }
    Iterator<Recipe> it = getServer().recipeIterator();
    while (it.hasNext()) {
      Recipe recipe = it.next();
      ItemStack resulting_item = recipe.getResult();
      if ( // !ench_gold_app_craftable_ &&
          isEnchantedGoldenApple(resulting_item)) {
        it.remove();
        info("Enchanted Golden Apple Recipe disabled");
      }
    }
  }

  // EventHandler registered in onPlayerInteractAll
  public void onPlayerEatGoldenApple(PlayerInteractEvent event) {
    // The event when eating is cancelled before even LOWEST fires when the
    //  player clicks on AIR.
    if (config_.getEnchGoldAppleEdible()) {
      return;
    }
    Player player = event.getPlayer();
    Inventory inventory = player.getInventory();
    ItemStack item = event.getItem();
    replaceEnchantedGoldenApple(
        player.getName(), item, inventory.getMaxStackSize());
  }

  // ================================================
  // Enchanted Book

  public boolean isNormalBook(ItemStack item) {
    if (item == null) {
      return false;
    }
    Material material = item.getType();
    return material.equals(Material.BOOK);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPrepareItemEnchantEvent(PrepareItemEnchantEvent event) {
    if (config_.getEnchBookCraftable()) {
        return;
    }
    ItemStack item = event.getItem();
    if (isNormalBook(item)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onEnchantItemEvent(EnchantItemEvent event) {
    if (config_.getEnchBookCraftable()) {
        return;
    }
    ItemStack item = event.getItem();
    if (isNormalBook(item)) {
      event.setCancelled(true);
      Player player = event.getEnchanter();
      warning(
          "Prevented book enchant. This should not trigger. Watch player " +
          player.getName());
    }
  }

  // ================================================
  // Stop Cobble generation from lava+water

  private static final BlockFace[] faces_ = new BlockFace[] {
      BlockFace.NORTH,
      BlockFace.SOUTH,
      BlockFace.EAST,
      BlockFace.WEST,
      BlockFace.UP,
      BlockFace.DOWN
    };


  private BlockFace WaterAdjacentLava(Block lava_block) {
    for (BlockFace face : faces_) {
      Block block = lava_block.getRelative(face);
      Material material = block.getType();
      if (material.equals(Material.WATER) ||
          material.equals(Material.STATIONARY_WATER)) {
        return face;
      }
    }
    return BlockFace.SELF;
  }

  public void ConvertLava(Block block) {
    int data = (int)block.getData();
    if (data == 0) {
      return;
    }
    Material material = block.getType();
    if (!material.equals(Material.LAVA) &&
        !material.equals(Material.STATIONARY_LAVA)) {
      return;
    }
    if (isLavaSourceNear(block, 3)) {
      return;
    }
    BlockFace face = WaterAdjacentLava(block);
    if (face == BlockFace.SELF) {
      return;
    }
    block.setType(Material.AIR);
  }

  public boolean isLavaSourceNear(Block block, int ttl) {
    int data = (int)block.getData();
    if (data == 0) {
      Material material = block.getType();
      if (material.equals(Material.LAVA) ||
          material.equals(Material.STATIONARY_LAVA)) {
        return true;
      }
    }
    if (ttl <= 0) {
      return false;
    }
    for (BlockFace face : faces_) {
      Block child = block.getRelative(face);
      if (isLavaSourceNear(child, ttl - 1)) {
        return true;
      }
    }
    return false;
  }

  public void LavaAreaCheck(Block block, int ttl) {
    ConvertLava(block);
    if (ttl <= 0) {
      return;
    }
    for (BlockFace face : faces_) {
      Block child = block.getRelative(face);
      LavaAreaCheck(child, ttl - 1);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
    if (config_.getCobbleFromLavaEnabled()) {
      return;
    }
    Block block = event.getBlock();
    LavaAreaCheck(block, config_.getCobbleFromLavaScanRadius());
  }

  // ================================================
  // Counteract 1.4.6 protection enchant nerf

  @EventHandler(priority = EventPriority.LOWEST) // ignoreCancelled=false
  public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
    if (!config_.getScaleProtectionEnchant()) {
        return;
    }
    int damage = event.getDamage();
    if (damage <= 0) {
      return;
    }
    DamageCause cause = event.getCause();
    if (!cause.equals(DamageCause.ENTITY_ATTACK) &&
            !cause.equals(DamageCause.PROJECTILE)) {
        return;
    }
    Entity entity = event.getEntity();
    if (!(entity instanceof Player)) {
      return;
    }
    Player defender = (Player)entity;
    PlayerInventory inventory = defender.getInventory();
    int enchant_level = 0;
    for (ItemStack armor : inventory.getArmorContents()) {
      enchant_level += armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
    }
    int damage_adjustment = 0;
    if (enchant_level >= 3 && enchant_level <= 6) {
      // 0 to 2
      damage_adjustment = prng_.nextInt(3);
    } else if (enchant_level >= 7 && enchant_level <= 10) {
      // 0 to 3
      damage_adjustment = prng_.nextInt(4);
    } else if (enchant_level >= 11 && enchant_level <= 14) {
      // 1 to 4
      damage_adjustment = prng_.nextInt(4) + 1;
    } else if (enchant_level >= 15) {
      // 2 to 4
      damage_adjustment = prng_.nextInt(3) + 2;
    }
    damage = Math.max(damage - damage_adjustment, 0);
    event.setDamage(damage);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    player.setMaxHealth(config_.getMaxHealth());
  }

  // ================================================
  // Prevent entity dup bug
  // From https://github.com/intangir/EventBlocker

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPistonPushRail(BlockPistonExtendEvent e) {
    if (!config_.getFixRailDupBug()) {
      return;
    }
    for (Block b : e.getBlocks()) {
      Material t = b.getType();
      if (t == Material.RAILS ||
          t == Material.POWERED_RAIL ||
          t == Material.DETECTOR_RAIL) {
        e.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onRailPlace(BlockPlaceEvent e) {
    if (!config_.getFixRailDupBug()) {
      return;
    }
    Block b = e.getBlock();
    Material t = b.getType();
    if (t == Material.RAILS ||
        t == Material.POWERED_RAIL ||
        t == Material.DETECTOR_RAIL) {
      for (BlockFace face : faces_) {
        t = b.getRelative(face).getType();
        if (t == Material.PISTON_STICKY_BASE ||
            t == Material.PISTON_EXTENSION ||
            t == Material.PISTON_MOVING_PIECE ||
            t == Material.PISTON_BASE) {
          e.setCancelled(true);
          return;
        }
      }
    }
  }

  //================================================
  // Give introduction book to n00bs

  @EventHandler
  public void OnPlayerFirstJoin(PlayerJoinEvent event){
    Player player = event.getPlayer();
    if (player.hasPlayedBefore()) {
      return;
    }
    giveN00bBook(player);
  }

  public void giveN00bBook(Player player) {
    Inventory inv = player.getInventory();
    inv.addItem(createN00bBook());
  }

  public ItemStack createN00bBook() {
    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta sbook = (BookMeta)book.getItemMeta();
    sbook.setTitle(config_.getTitle());
    sbook.setAuthor(config_.getAuthor());
    sbook.setPages(config_.getPages());
    book.setItemMeta(sbook);
    return book;
  }

  //================================================
  // Fix player in vehicle logout bug

  private static final int air_material_id_ = Material.AIR.getId();

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onDisallowVehicleLogout(PlayerQuitEvent event) {
    if (!config_.getFixVehicleLogoutBug()) {
      return;
    }
    Player player = event.getPlayer();
    Entity vehicle = player.getVehicle();
    if (vehicle == null) {
      return;
    }
    Location loc = vehicle.getLocation();
    World world = loc.getWorld();
    // Vehicle data has been cached, now safe to kick the player out
    player.leaveVehicle();

    // First attempt to place the player just above the vehicle
    // Normalize the location. Add 1 to Y so it is just above the minecart
    loc.setX(Math.floor(loc.getX()) + 0.5000);
    loc.setY(Math.floor(loc.getY()) + 1.0000);
    loc.setZ(Math.floor(loc.getZ()) + 0.5000);
    Block block = world.getBlockAt(loc);
    if (block.getTypeId() == air_material_id_) {
      block = block.getRelative(BlockFace.UP);
      if (block.getTypeId() == air_material_id_) {
        player.teleport(loc);
        Humbug.info(String.format(
            "Vehicle logout [%s]: Teleported to %s",
            player.getName(), loc.toString()));
        return;
      }
    }

    // The space above the cart was blocked. Scan from the top of the world down
    //  and find 4 vertically contiguous AIR blocks resting above a non-AIR
    //  block. The size is 4 to provide players a way to prevent griefers from
    //  teleporting into small spaces (2 or 3 blocks high).
    Environment world_type = world.getEnvironment();
    int max_height;
    if (world_type == Environment.NETHER) {
      max_height = 126;
    } else {
      max_height = world.getMaxHeight() - 2;
    }
    // Create a sliding window of block types and track how many of those
    //  are AIR. Keep fetching the block below the current block to move down.
    int air_count = 0;
    LinkedList<Integer> air_window = new LinkedList<Integer>();
    loc.setY((float)max_height);
    block = world.getBlockAt(loc);
    for (int i = 0; i < 4; ++i) {
      int block_type = block.getTypeId();
      if (block_type == air_material_id_) {
        ++air_count;
      }
      air_window.addLast(block_type);
      block = block.getRelative(BlockFace.DOWN);
    }

    // Now that the window is prepared, scan down the Y-axis.
    // 3 to prevent bedrock pocket access
    while (block.getY() > 3) {
      int block_type = block.getTypeId();
      if (block_type != air_material_id_) {
        if (air_count == 4) {
          // Normalize the location on the block's center. Y+1 which is the
          //  first AIR above this block.
          loc = block.getLocation();
          loc.setX(Math.floor(loc.getX()) + 0.5000);
          loc.setY(Math.floor(loc.getY()) + 1.0000);
          loc.setZ(Math.floor(loc.getZ()) + 0.5000);
          player.teleport(loc);
          Humbug.info(String.format(
              "Vehicle logout [%s]: Teleported to %s",
              player.getName(), loc.toString()));
          return;
        }
      } else { // block_type == air_material_id_
        ++air_count;
      }
      air_window.addLast(block_type);
      if (air_window.removeFirst() == air_material_id_) {
        --air_count;
      }
      block = block.getRelative(BlockFace.DOWN);
    }

    // No space in this (x,z) column to teleport the player. Feed them
    //  to the lions.
    Humbug.info(String.format(
        "Vehicle logout [%s]: No space for teleport, killed",
        player.getName()));
    player.setHealth(0);
  }

  // ================================================
  // Playing records in jukeboxen? Gone

  // EventHandler registered in onPlayerInteract
  public void onRecordInJukebox(PlayerInteractEvent event) {
    if (!config_.getDisallowRecordPlaying()) {
      return;
    }
    Block cb = event.getClickedBlock();
    if (cb == null || cb.getType() != Material.JUKEBOX) {
      return;
    }
    ItemStack his = event.getItem();
    if(his != null && his.getType().isRecord()) {
      event.setCancelled(true);
    }
  }

  //=================================================
  // Water in the nether? Nope.

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent e) {
    if(!config_.getAllowWaterInNether()) {
      if( ( e.getBlockClicked().getBiome() == Biome.HELL )
          && ( e.getBucket() == Material.WATER_BUCKET ) ) {
        e.setCancelled(true);
        e.getItemStack().setType(Material.BUCKET);
        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 5, 1));
      }
    }
    if (config_.getIndestructibleEndPortals()) {
      Block baseBlock = e.getBlockClicked();
      BlockFace face = e.getBlockFace();
      Block block = baseBlock.getRelative(face);
      if (block.getType() == Material.ENDER_PORTAL) {
          e.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockFromToEvent(BlockFromToEvent e) {
    if(!config_.getAllowWaterInNether()) {
      if( e.getToBlock().getBiome() == Biome.HELL ) {
        if( ( e.getBlock().getType() == Material.WATER )
            || ( e.getBlock().getType() == Material.STATIONARY_WATER ) ) {
          e.setCancelled(true);
        }
      }
    }
    if (config_.getIndestructibleEndPortals()) {
      if (e.getToBlock().getType() == Material.ENDER_PORTAL) {
          e.setCancelled(true);
      }
    }
  }

  //=================================================
  // Bow shots cause slow debuff

  @EventHandler
  public void onEDBE(EntityDamageByEntityEvent event) {
    int rate = config_.getProjectileSlowChance();
    if (rate <= 0 || rate > 100) {
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    boolean damager_is_player_arrow = false;
    int chance_scaling = 0;
    Entity damager_entity = event.getDamager();
    if (damager_entity != null) {
      // public LivingEntity CraftArrow.getShooter()
      // Playing this game to not have to take a hard dependency on
      //  craftbukkit internals.
      try {
        Class<?> damager_class = damager_entity.getClass();
        if (damager_class.getName().endsWith(".CraftArrow")) {
          Method getShooter = damager_class.getMethod("getShooter");
          Object result = getShooter.invoke(damager_entity);
          if (result instanceof Player) {
            damager_is_player_arrow = true;
            String player_name = ((Player)result).getName();
            if (bow_level_.containsKey(player_name)) {
              chance_scaling = bow_level_.get(player_name);
            }
          }
        }
      } catch(Exception ex) {}
    }
    if (!damager_is_player_arrow) {
      return;
    }
    rate += chance_scaling * 5;
    int percent = prng_.nextInt(100);
    if (percent < rate){
      int ticks = config_.getProjectileSlowTicks();
      Player player = (Player)event.getEntity();
      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 1, false));
    }
  }

  // Used to track bow enchantment levels per player
  private Map<String, Integer> bow_level_ = new TreeMap<String, Integer>();

  @EventHandler
  public void onEntityShootBow(EntityShootBowEvent event) {
    if (!(event.getEntity() instanceof Player)) {
         return;
    }
    int ench_level = 0;
    ItemStack bow = event.getBow();
    Map<Enchantment, Integer> enchants = bow.getEnchantments();
    for (Enchantment ench : enchants.keySet()) {
      int tmp_ench_level = 0;
      if (ench.equals(Enchantment.KNOCKBACK) || ench.equals(Enchantment.ARROW_KNOCKBACK)) {
        tmp_ench_level = enchants.get(ench) * 2;
      } else if (ench.equals(Enchantment.ARROW_DAMAGE)) {
        tmp_ench_level = enchants.get(ench);
      }
      if (tmp_ench_level > ench_level) {
        ench_level = tmp_ench_level;
      }
    }
    bow_level_.put(
        ((Player)event.getEntity()).getName(),
        ench_level);
  }
  
  // ================================================
  // Change ender pearl velocity!
  
  @EventHandler
  public void onEnderPearlThrow(ProjectileLaunchEvent event) {
    Entity entity = (Entity)event.getEntity();
    if (!(entity instanceof EnderPearl)) {
      return;
    }
    double adjustment = config_.getEnderPearlLaunchVelocity();
    if (adjustment < 1.00001 && adjustment > 0.99999) {
      return;
    }
    entity.setVelocity(entity.getVelocity().multiply(adjustment));
  }

  // ================================================
  // Ender pearl cooldown timer

  private class PearlTeleportInfo {
    public long last_teleport;
    public long teleport_count;
    public boolean notified_player;
  }

  private Map<String, PearlTeleportInfo> pearl_teleport_info_
      = new TreeMap<String, PearlTeleportInfo>();

  // EventHandler registered in onPlayerInteractAll
  public void onPlayerPearlTeleport(PlayerInteractEvent event) {
    if (!config_.getThrottlePearlTeleport()) {
      return;
    }
    if (event.getItem() == null || !event.getItem().getType().equals(Material.ENDER_PEARL)) {
      return;
    }
    Action action = event.getAction();
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    long current_time = System.currentTimeMillis();
    String player_name = event.getPlayer().getName();
    PearlTeleportInfo teleport_info = pearl_teleport_info_.get(player_name);
    if (teleport_info == null) {
      teleport_info = new PearlTeleportInfo();
      teleport_info.teleport_count = 1;
      teleport_info.last_teleport = current_time;
      teleport_info.notified_player = false;
    } else {
      long time_diff = current_time - teleport_info.last_teleport;
      long block_window = teleport_info.teleport_count * 5000;
      if (block_window > 60000) {
        block_window = 60000;
      }
      if (block_window > time_diff) {
        if (!teleport_info.notified_player) {
          event.getPlayer().sendMessage(String.format(
              "Pearl Teleport Cooldown: %ds",
              (block_window - time_diff + 500) / 1000));
          teleport_info.notified_player = true;
        }
        event.setCancelled(true);
      } else if (block_window + 10000 > time_diff) {
        teleport_info.teleport_count++;
        teleport_info.last_teleport = current_time;
        teleport_info.notified_player = false;
      } else {
        teleport_info.teleport_count = 1;
        teleport_info.last_teleport = current_time;
        teleport_info.notified_player = false;
      }
    }
    pearl_teleport_info_.put(player_name, teleport_info);
  }

  // ================================================
  // BottleO refugees

  // Changes the yield from an XP bottle
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onExpBottleEvent(ExpBottleEvent event) {
    if (config_.getDisableExperience()) {
      ((Player) event.getEntity().getShooter()).giveExp(config_.getXPPerBottle());
      event.setExperience(0);
    } else {
      event.setExperience(config_.getXPPerBottle());
    }
  }

  // Diables all XP gain except when manually changed via code.
  @EventHandler
  public void onPlayerExpChangeEvent(PlayerExpChangeEvent event) {
    if (config_.getDisableExperience()) {
      event.setAmount(0);
    }
  }

  // ================================================
  // Find the end portals

  public static final int ender_portal_id_ = Material.ENDER_PORTAL.getId();
  public static final int ender_portal_frame_id_ = Material.ENDER_PORTAL_FRAME.getId();
  private Set<Long> end_portal_scanned_chunks_ = new TreeSet<Long>();

  @EventHandler
  public void onFindEndPortals(ChunkLoadEvent event) {
    if (config_.getFindEndPortals() == null) {
      return;
    }
    World world = event.getWorld();
    if (!world.getName().equalsIgnoreCase(config_.getFindEndPortals())) {
      return;
    }
    Chunk chunk = event.getChunk();
    long chunk_id = (long)chunk.getX() << 32L + (long)chunk.getZ();
    if (end_portal_scanned_chunks_.contains(chunk_id)) {
      return;
    }
    end_portal_scanned_chunks_.add(chunk_id);
    int chunk_x = chunk.getX() * 16;
    int chunk_end_x = chunk_x + 16;
    int chunk_z = chunk.getZ() * 16;
    int chunk_end_z = chunk_z + 16;
    int max_height = 0;
    for (int x = chunk_x; x < chunk_end_x; x += 3) {
      for (int z = chunk_z; z < chunk_end_z; ++z) {
        int height = world.getHighestBlockYAt(x, z);
        if (height > max_height) {
          max_height = height;
        }
      }
    }
    for (int y = 1; y <= max_height; ++y) {
      int z_adj = 0;
      for (int x = chunk_x; x < chunk_end_x; ++x) {
        for (int z = chunk_z + z_adj; z < chunk_end_z; z += 3) {
          int block_type = world.getBlockTypeIdAt(x, y, z);
          if (block_type == ender_portal_id_ || block_type == ender_portal_frame_id_) {
            info(String.format("End portal found at %d,%d", x, z));
            return;
          }
        }
        // This funkiness results in only searching 48 of the 256 blocks on
        //  each y-level. 81.25% fewer blocks checked.
        ++z_adj;
        if (z_adj >= 3) {
          z_adj = 0;
          x += 2;
        }
      }
    }
  }

  // ================================================
  // Prevent inventory access while in a vehicle, unless it's the Player's

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPreventVehicleInvOpen(InventoryOpenEvent event) {
    // Cheap break-able conditional statement
    while (config_.getPreventVehicleInventoryOpen()) {
      HumanEntity human = event.getPlayer();
      if (!(human instanceof Player)) {
        break;
      }
      if (!human.isInsideVehicle()) {
        break;
      }
      InventoryHolder holder = event.getInventory().getHolder();
      if (holder == human) {
        break;
      }
      event.setCancelled(true);
      break;
    }
    if (config_.getPreventOpeningContainerCarts() && !event.isCancelled()) {
      InventoryHolder holder = event.getInventory().getHolder();
      if (holder instanceof StorageMinecart || holder instanceof HopperMinecart) {
        event.setCancelled(true);
      }
    }
  }

  // ================================================
  // General

  public void onEnable() {
    registerEvents();
    registerCommands();
    loadConfiguration();
    removeRecipies();
    global_instance_ = this;
    info("Enabled");
  }

  public boolean isInitiaized() {
    return global_instance_ != null;
  }

  public boolean toBool(String value) {
    if (value.equals("1") || value.equalsIgnoreCase("true")) {
      return true;
    }
    return false;
  }

  public int toInt(String value, int default_value) {
    try {
      return Integer.parseInt(value);
    } catch(Exception e) {
      return default_value;
    }
  }
  
  public double toDouble(String value, double default_value) {
    try {
      return Double.parseDouble(value);
    } catch(Exception e) {
      return default_value;
    }
  }

  public int toMaterialId(String value, int default_value) {
    try {
      return Integer.parseInt(value);
    } catch(Exception e) {
      Material mat = Material.matchMaterial(value);
      if (mat != null) {
        return mat.getId();
      }
    }
    return default_value;
  }

  public boolean onCommand(
      CommandSender sender,
      Command command,
      String label,
      String[] args) {
    if (sender instanceof Player
        && command.getName().equals("introbook")) {
      Player sendBookTo = (Player)sender;
      if (args.length >= 1) {
        Player possible = Bukkit.getPlayerExact(args[0]);
        if (possible != null) {
          sendBookTo = possible;
        }
      }
      giveN00bBook(sendBookTo);
      return true;
    }
    if (!(sender instanceof ConsoleCommandSender) ||
        !command.getName().equals("humbug") ||
        args.length < 1) {
      return false;
    }
    String option = args[0];
    String value = null;
    String subvalue = null;
    boolean set = false;
    boolean subvalue_set = false;
    String msg = "";
    if (args.length > 1) {
      value = args[1];
      set = true;
    }
    if (args.length > 2) {
      subvalue = args[2];
      subvalue_set = true;
    }
    if (option.equals("debug")) {
      if (set) {
        config_.setDebug(toBool(value));
      }
      msg = String.format("debug = %s", config_.getDebug());
    } else if (option.equals("anvil")) {
      if (set) {
        config_.setAnvilEnabled(toBool(value));
      }
      msg = String.format("anvil = %s", config_.getAnvilEnabled());
    } else if (option.equals("ender_chest")) {
      if (set) {
        config_.setEnderChestEnabled(toBool(value));
      }
      msg = String.format("ender_chest = %s", config_.getEnderChestEnabled());
    } else if (option.equals("ender_chests_placeable")) {
      if (set) {
        config_.setEnderChestsPlaceable(toBool(value));
      }
      msg = String.format("ender_chests_placeable = %s", config_.getEnderChestsPlaceable());
    } else if (option.equals("villager_trades")) {
      if (set) {
        config_.setVillagerTradesEnabled(toBool(value));
      }
      msg = String.format("villager_trades = %s", config_.getVillagerTradesEnabled());
    } else if (option.equals("portalcreate")) {
      if (set) {
        config_.setPortalCreateEnabled(toBool(value));
      }
      msg = String.format("portalcreate = %s", config_.getPortalCreateEnabled());
    } else if (option.equals("enderdragon")) {
      if (set) {
        config_.setEnderDragonEnabled(toBool(value));
      }
      msg = String.format("enderdragon = %s", config_.getEnderDragonEnabled());
    } else if (option.equals("joinquitkick")) {
      if (set) {
        config_.setJoinQuitKickEnabled(toBool(value));
      }
      msg = String.format("joinquitkick = %s", config_.getJoinQuitKickEnabled());
    } else if (option.equals("deathpersonal")) {
      if (set) {
        config_.setDeathMessagePersonalEnabled(toBool(value));
      }
      msg = String.format("deathpersonal = %s", config_.getDeathMessagePersonalEnabled());
    } else if (option.equals("deathannounce")) {
      if (set) {
        config_.setDeathAnnounceEnabled(toBool(value));
      }
      msg = String.format("deathannounce = %s", config_.getDeathAnnounceEnabled());
    } else if (option.equals("deathred")) {
      if (set) {
        config_.setDeathMessageRedEnabled(toBool(value));
      }
      msg = String.format("deathred = %s", config_.getDeathMessageRedEnabled());
    } else if (option.equals("deathlog")) {
      if (set) {
        config_.setDeathLoggingEnabled(toBool(value));
      }
      msg = String.format("deathlog = %s", config_.getDeathLoggingEnabled());
    } else if (option.equals("endergrief")) {
      if (set) {
        config_.setEndermenGriefEnabled(toBool(value));
      }
      msg = String.format("endergrief = %s", config_.getEndermenGriefEnabled());
    } else if (option.equals("unlimitedcauldron")) {
      if (set) {
        config_.setUnlimitedCauldronEnabled(toBool(value));
      }
      msg = String.format("unlimitedcauldron = %s", config_.getUnlimitedCauldronEnabled());
    } else if (option.equals("quartz_gravel_percentage")) {
      if (set) {
        config_.setQuartzGravelPercentage(toInt(value, config_.getQuartzGravelPercentage()));
      }
      msg = String.format("quartz_gravel_percentage = %d", config_.getQuartzGravelPercentage());
    } else if (option.equals("wither")) {
      if (set) {
        config_.setWitherEnabled(toBool(value));
      }
      msg = String.format("wither = %s", config_.getWitherEnabled());
    } else if (option.equals("wither_explosions")) {
      if (set) {
        config_.setWitherExplosionsEnabled(toBool(value));
      }
      msg = String.format(
          "wither_explosions = %s", config_.getWitherExplosionsEnabled());
    } else if (option.equals("wither_insta_break")) {
      if (set) {
        config_.setWitherInstaBreakEnabled(toBool(value));
      }
      msg = String.format(
          "wither_insta_break = %s", config_.getWitherInstaBreakEnabled());
    } else if (option.equals("ench_gold_app_edible")) {
      if (set) {
        config_.setEnchGoldAppleEdible(toBool(value));
      }
      msg = String.format(
          "ench_gold_app_edible = %s", config_.getEnchGoldAppleEdible());
    } else if (option.equals("ench_gold_app_craftable")) {
      if (set) {
        config_.setEnchGoldAppleCraftable(toBool(value));
      }
      msg = String.format(
          "ench_gold_app_craftable = %s", config_.getEnchGoldAppleCraftable());
    } else if (option.equals("cobble_from_lava")) {
      if (set) {
        config_.setCobbleFromLavaEnabled(toBool(value));
      }
      msg = String.format("cobble_from_lava = %s", config_.getCobbleFromLavaEnabled());
    } else if (option.equals("cobble_from_lava_scan_radius")) {
      if (set) {
        config_.setCobbleFromLavaScanRadius(toInt(value, config_.getCobbleFromLavaScanRadius()));
      }
      msg = String.format("cobble_from_lava_scan_radius = %d", config_.getCobbleFromLavaScanRadius());
    } else if (option.equals("ench_book_craftable")) {
      if (set) {
        config_.setEnchBookCraftable(toBool(value));
      }
      msg = String.format("ench_book_craftable = %s", config_.getEnchBookCraftable());
    } else if (option.equals("scale_protection_enchant")) {
      if (set) {
        config_.setScaleProtectionEnchant(toBool(value));
      }
      msg = String.format(
          "scale_protection_enchant = %s", config_.getScaleProtectionEnchant());
    } else if (option.equals("fix_rail_dup_bug")) {
      if (set) {
        config_.setFixRailDupBug(toBool(value));
      }
      msg = String.format("fix_rail_dup_bug = %s", config_.getFixRailDupBug());
    } else if (option.equals("fix_vehicle_logout_bug")) {
      if (set) {
        config_.setFixVehicleLogoutBug(toBool(value));
      }
      msg = String.format("fix_vehicle_logout_bug = %s", config_.getFixVehicleLogoutBug());
    } else if (option.equals("wither_skull_drop_rate")) {
      if (set) {
        config_.setWitherSkullDropRate(toInt(value, config_.getWitherSkullDropRate()));
      }
      msg = String.format("wither_skull_drop_rate = %d", config_.getWitherSkullDropRate());
    } else if (option.equals("extra_wither_skele_spawn_rate")) {
      if (set) {
        config_.setExtraWitherSkeleSpawnRate(toInt(value, config_.getExtraWitherSkeleSpawnRate()));
      }
      msg = String.format("extra_wither_skele_spawn_rate = %d", config_.getExtraWitherSkeleSpawnRate());
    } else if (option.equals("extra_ghast_spawn_rate")) {
      if (set) {
        config_.setExtraGhastSpawnRate(toInt(value, config_.getExtraGhastSpawnRate()));
      }
      msg = String.format("extra_ghast_spawn_rate = %d", config_.getExtraGhastSpawnRate());
    } else if (option.equals("loot_multiplier")) {
      String entity_type = "generic";
      if (set && subvalue_set) {
        entity_type = value;
        value = subvalue;
      }
      if (set) {
        config_.setLootMultiplier(
                entity_type, toInt(value, config_.getLootMultiplier(entity_type)));
      }
      msg = String.format(
          "loot_multiplier(%s) = %d", entity_type, config_.getLootMultiplier(entity_type));
    } else if (option.equals("disable_experience")) {
      if (set) {
        config_.setDisableExperience(toBool(value));
      }
      msg = String.format("disable_experience = %d", config_.getDisableExperience());
    } else if (option.equals("xp_per_bottle")) {
      if (set) {
        config_.setXPPerBottle(toInt(value, config_.getXPPerBottle()));
      }
      msg = String.format("xp_per_bottle = %d", config_.getXPPerBottle());
    } else if (option.equals("player_max_health")) {
      if (set) {
        config_.setMaxHealth(toInt(value, config_.getMaxHealth()));
      }
      msg = String.format("player_max_health = %d", config_.getMaxHealth());
    } else if (option.equals("fix_teleport_glitch")) {
      if (set) {
        config_.setTeleportFixEnabled(toBool(value));
      }
      msg = String.format("fix_teleport_glitch = %s", config_.getTeleportFixEnabled());
    } else if (option.equals("ender_pearl_teleportation")) {
      if (set) {
        config_.setEnderPearlTeleportationEnabled(toBool(value));
      }
      msg = String.format("ender_pearl_teleportation = %s", config_.getEnderPearlTeleportationEnabled());
    } else if (option.equals("ender_pearl_teleportation_throttled")) {
      if (set) {
        config_.setThrottlePearlTeleport(toBool(value));
      }
      msg = String.format("ender_pearl_teleportation_throttled = %s", config_.getThrottlePearlTeleport());
    } else if (option.equals("ender_pearl_launch_velocity")) {
      if (set) {
        config_.setEnderPearlLaunchVelocity(toDouble(value, 1.0000));
      }
      msg = String.format("ender_pearl_launch_velocity = %s", config_.getEnderPearlLaunchVelocity());
    } else if (option.equals("disallow_record_playing")) {
      if (set) {
        config_.setDisallowRecordPlaying(toBool(value));
      }
      msg = String.format("disallow_record_playing = %s", config_.getDisallowRecordPlaying());
    } else if (option.equals("allow_dye_sheep")) {
      if (set) {
        config_.setAllowDyeSheep(toBool(value));
      }
      msg = String.format("allow_dye_sheep = %s", config_.getAllowDyeSheep());
    } else if (option.equals("allow_water_in_nether")) {
      if (set) {
        config_.setAllowWaterInNether(toBool(value));
      }
      msg = String.format("allow_water_in_nether = %s", config_.getAllowWaterInNether());
    } else if (option.equals("indestructible_end_portals")) {
      if (set) {
        config_.setIndestructibleEndPortals(toBool(value));
      }
      msg = String.format("indestructible_end_portals = %s", config_.getIndestructibleEndPortals());
    } else if (option.equals("prevent_vehicle_inventory_open")) {
      if (set) {
        config_.setPreventVehicleInventoryOpen(toBool(value));
      }
      msg = String.format("prevent_vehicle_inventory_open = %s", config_.getPreventVehicleInventoryOpen());
    } else if (option.equals("prevent_opening_container_carts")) {
      if (set) {
        config_.setPreventOpeningContainerCarts(toBool(value));
      }
      msg = String.format("prevent_opening_container_carts = %s", config_.getPreventOpeningContainerCarts());
    } else if (option.equals("find_end_portals")) {
      if (set) {
        config_.setFindEndPortals(value);
      }
      msg = String.format("find_end_portals = %s", config_.getFindEndPortals());
    } else if (option.equals("projectile_slow_chance")) {
      if (set) {
        config_.setProjectileSlowChance(toInt(value, config_.getProjectileSlowChance()));
      }
      msg = String.format("projectile_slow_chance = %d", config_.getProjectileSlowChance());
    } else if (option.equals("projectile_slow_ticks")) {
      if (set) {
        config_.setProjectileSlowTicks(toInt(value, config_.getProjectileSlowTicks()));
      }
      msg = String.format("projectile_slow_ticks = %d", config_.getProjectileSlowTicks());
    } else if (option.equals("remove_mob_drops")) {
      if (set && subvalue_set) {
        if (value.equals("add")) {
          config_.addRemoveItemDrop(toMaterialId(subvalue, -1));
        } else if (value.equals("del")) {
          config_.removeRemoveItemDrop(toMaterialId(subvalue, -1));
        }
      }
      msg = String.format("remove_mob_drops = %s", config_.toDisplayRemoveItemDrops());
    } else if (option.equals("save")) {
      config_.save();
      msg = "Configuration saved";
    } else if (option.equals("reload")) {
      config_.reload();
      msg = "Configuration loaded";
    } else {
      msg = String.format("Unknown option %s", option);
    }
    sender.sendMessage(msg);
    return true;
  }

  public void registerCommands() {
    ConsoleCommandSender console = getServer().getConsoleSender();
    console.addAttachment(this, "humbug.console", true);
  }

  private void registerEvents() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  private void loadConfiguration() {
    config_ = Config.initialize(this);
  }
}
