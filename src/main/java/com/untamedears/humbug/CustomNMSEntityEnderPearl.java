package com.untamedears.humbug;

import java.util.logging.Logger;

import net.minecraft.server.v1_5_R2.EntityEnderPearl;
import net.minecraft.server.v1_5_R2.EntityLiving;
import net.minecraft.server.v1_5_R2.World;

public class CustomNMSEntityEnderPearl extends EntityEnderPearl {

  public CustomNMSEntityEnderPearl(World world) {
    super(world);
  }

  public CustomNMSEntityEnderPearl(World world, EntityLiving living) {
    super(world, living);
  }

  public static final double multiplier_ = 0.90F;
  public static final double y_adjust_ = 0.07F;

  public void l_() {
    super.l_();
    this.motX *= multiplier_;
    this.motY *= multiplier_;
    this.motZ *= multiplier_;
    this.motY -= y_adjust_;
  }
}
