package com.untamedears.humbug;

import java.util.logging.Logger;

import net.minecraft.server.v1_6_R3.EntityEnderPearl;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.World;

public class CustomNMSEntityEnderPearl extends EntityEnderPearl {

  public CustomNMSEntityEnderPearl(World world) {
    super(world);
    y_adjust_ = 0.030000F;
  }

  public CustomNMSEntityEnderPearl(World world, EntityLiving living, double gravity) {
    super(world, living);
    y_adjust_ = gravity;
  }

  public final double y_adjust_;  // Default 0.03F

  protected float e() {
    return (float)y_adjust_;
  }
}
