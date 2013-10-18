package com.untamedears.humbug;

import java.util.logging.Logger;

import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.ItemEnderPearl;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.World;

import com.untamedears.humbug.Config;
import com.untamedears.humbug.CustomNMSEntityEnderPearl;

public class CustomNMSItemEnderPearl extends ItemEnderPearl {
  private Config cfg_;

  public CustomNMSItemEnderPearl(int i, Config cfg) {
    super(i);
    cfg_ = cfg;
  }

  public ItemStack a(
      ItemStack itemstack,
      World world,
      EntityHuman entityhuman) {
    if (entityhuman.abilities.canInstantlyBuild) {
      return itemstack;
    } else if (entityhuman.vehicle != null) {
      return itemstack;
    } else {
      --itemstack.count;
      world.makeSound(
          entityhuman,
          "random.bow",
          0.5F,
          0.4F / (f.nextFloat() * 0.4F + 0.8F));
      if (!world.isStatic) {
        double gravity = cfg_.get("ender_pearl_gravity").getDouble();
        world.addEntity(new CustomNMSEntityEnderPearl(world, entityhuman, gravity));
      }
      return itemstack;
    }
  }
}
