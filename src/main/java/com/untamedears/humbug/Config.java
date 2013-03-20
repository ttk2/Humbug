package com.untamedears.humbug;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.untamedears.humbug.Humbug;

public class Config {
  private static Config global_instance_ = null;

  // ================================================
  // Configuration defaults
  private static final boolean debug_log_ = false;
  private static final boolean anvil_enabled_ = false;
  private static final boolean ender_chest_enabled_ = false;
  private static final boolean villager_trades_enabled_ = false;
  private static final boolean portalcreate_enabled_ = true;
  private static final boolean enderdragon_enabled_ = true;
  private static final boolean joinquitkick_enabled_ = true;
  private static final boolean deathpersonal_enabled_ = true;
  private static final boolean deathannounce_enabled_ = true;
  private static final boolean deathred_enabled_ = true;
  private static final boolean endergrief_enabled_ = false;
  private static final boolean wither_enabled_ = true;
  private static final boolean wither_explosions_enabled_ = false;
  private static final boolean wither_insta_break_enabled_ = false;
  private static final boolean cobble_from_lava_enabled_ = false;
  private static final int cobble_from_lava_scan_radius_ = 0;
  private static final boolean ench_book_craftable_ = false;
  private static final boolean scale_protection_enchant_ = true;
  private static final boolean fix_rail_dup_bug_ = true;
  private static final boolean fix_vehicle_logout_bug_ = true;
  private static final int player_max_health_ = 20;
  // For Enchanted GOLDEN_APPLES
  private static final boolean ench_gold_app_edible_ = false;
  private static final boolean ench_gold_app_craftable_ = false;

  private static Config get() {
    return global_instance_;
  }

  public static Config initialize(Plugin plugin) {
    if (global_instance_ == null) {
      global_instance_ = new Config(plugin);
      global_instance_.load();
    }
    return global_instance_;
  }

  private FileConfiguration config_ = null;
  private Plugin plugin_ = null;

  public Config(Plugin plugin) {
    plugin_ = plugin;
  }

  public void load() {
    plugin_.reloadConfig();
    FileConfiguration config = plugin_.getConfig();
    config.options().copyDefaults(true);
    config_ = config;
  }

  public void reload() {
    plugin_.reloadConfig();
  }

  public void save() {
    plugin_.saveConfig();
  }

  public boolean getDebug() {
    return config_.getBoolean("debug", debug_log_);
  }

  public void setDebug(boolean value) {
    config_.set("debug", value);
  }

  public boolean getAnvilEnabled() {
    return config_.getBoolean("anvil", anvil_enabled_);
  }

  public void setAnvilEnabled(boolean value) {
    config_.set("anvil", value);
  }

  public boolean getEnderChestEnabled() {
    return config_.getBoolean("ender_chest", ender_chest_enabled_);
  }

  public void setEnderChestEnabled(boolean value) {
    config_.set("ender_chest", value);
  }

  public boolean getVillagerTradesEnabled() {
    return config_.getBoolean("villager_trades", villager_trades_enabled_);
  }

  public void setVillagerTradesEnabled(boolean value) {
    config_.set("villager_trades", value);
  }

  public boolean getPortalCreateEnabled() {
    return config_.getBoolean("portalcreate", portalcreate_enabled_);
  }

  public void setPortalCreateEnabled(boolean value) {
    config_.set("portalcreate", value);
  }

  public boolean getEnderDragonEnabled() {
    return config_.getBoolean("enderdragon", enderdragon_enabled_);
  }

  public void setEnderDragonEnabled(boolean value) {
    config_.set("enderdragon", value);
  }

  public boolean getJoinQuitKickEnabled() {
    return config_.getBoolean("joinquitkick", joinquitkick_enabled_);
  }

  public void setJoinQuitKickEnabled(boolean value) {
    config_.set("joinquitkick", value);
  }

  public boolean getDeathMessagePersonalEnabled() {
    return config_.getBoolean("deathpersonal", deathpersonal_enabled_);
  }

  public void setDeathMessagePersonalEnabled(boolean value) {
    config_.set("deathpersonal", value);
  }

  public boolean getDeathMessageEnabled() {
    return config_.getBoolean("deathannounce", deathannounce_enabled_);
  }

  public void setDeathMessageEnabled(boolean value) {
    config_.set("deathannounce", value);
  }

  public boolean getDeathMessageRedEnabled() {
    return config_.getBoolean("deathred", deathred_enabled_);
  }

  public void setDeathMessageRedEnabled(boolean value) {
    config_.set("deathred", value);
  }

  public boolean getEndermenGriefEnabled() {
    return config_.getBoolean("endergrief", endergrief_enabled_);
  }

  public void setEndermenGriefEnabled(boolean value) {
    config_.set("endergrief", value);
  }

  public boolean getWitherEnabled() {
    return config_.getBoolean("wither", wither_enabled_);
  }

  public void setWitherEnabled(boolean value) {
    config_.set("wither", value);
  }

  public boolean getWitherExplosionsEnabled() {
    return config_.getBoolean("wither_explosions", wither_explosions_enabled_);
  }

  public void setWitherExplosionsEnabled(boolean value) {
    config_.set("wither_explosions", value);
  }

  public boolean getWitherInstaBreakEnabled() {
    return config_.getBoolean("wither_insta_break", wither_insta_break_enabled_);
  }

  public void setWitherInstaBreakEnabled(boolean value) {
    config_.set("wither_insta_break", value);
  }

  public boolean getEnchGoldAppleEdible() {
    return config_.getBoolean("ench_gold_app_edible", ench_gold_app_edible_);
  }

  public void setEnchGoldAppleEdible(boolean value) {
    config_.set("ench_gold_app_edible", value);
  }

  public boolean getEnchGoldAppleCraftable() {
    return config_.getBoolean("ench_gold_app_craftable", ench_gold_app_craftable_);
  }

  public void setEnchGoldAppleCraftable(boolean value) {
    config_.set("ench_gold_app_craftable", value);
  }

  public boolean getCobbleFromLavaEnabled() {
    return config_.getBoolean("cobble_from_lava", cobble_from_lava_enabled_);
  }

  public void setCobbleFromLavaEnabled(boolean value) {
    config_.set("cobble_from_lava", value);
  }

  public int getCobbleFromLavaScanRadius() {
    return config_.getInt("cobble_from_lava_scan_radius", cobble_from_lava_scan_radius_);
  }

  public void setCobbleFromLavaScanRadius(int value) {
    if (value < 0) {
      value = 0;
      Humbug.warning("cobble_from_lava_scan_radius adjusted to 0");
    } else if (value > 20) {  // 8000 blocks to scan at 20
      value = 20;
      Humbug.warning("cobble_from_lava_scan_radius adjusted to 20");
    }
    config_.set("cobble_from_lava_scan_radius", value);
  }

  public boolean getEnchBookCraftable() {
    return config_.getBoolean("ench_book_craftable", ench_book_craftable_);
  }

  public void setEnchBookCraftable(boolean value) {
    config_.set("ench_book_craftable", value);
  }

  public boolean getScaleProtectionEnchant() {
    return config_.getBoolean("scale_protection_enchant", scale_protection_enchant_);
  }

  public void setScaleProtectionEnchant(boolean value) {
    config_.set("scale_protection_enchant", value);
  }

  public boolean getFixRailDupBug() {
    return config_.getBoolean("fix_rail_dup_bug", fix_rail_dup_bug_);
  }

  public void setFixRailDupBug(boolean value) {
    config_.set("fix_rail_dup_bug", value);
  }

  public boolean getFixVehicleLogoutBug() {
    return config_.getBoolean("fix_vehicle_logout_bug", fix_vehicle_logout_bug_);
  }

  public void setFixVehicleLogoutBug(boolean value) {
    config_.set("fix_vehicle_logout_bug", value);
  }

  public int getMaxHealth() {
	return config_.getInt("player_max_health", player_max_health_);
  }

  public void setMaxHealth(int value) {
    config_.set("player_max_health", value);
  }
}
