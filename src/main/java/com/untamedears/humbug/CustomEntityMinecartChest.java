package com.untamedears.humbug;

import net.minecraft.server.v1_6_R3.EntityMinecartChest;
import net.minecraft.server.v1_6_R3.World;

public class CustomEntityMinecartChest extends EntityMinecartChest{
	private Config config_;
	public CustomEntityMinecartChest(World world, Config cfg_) {
		super(world);
		config_ = cfg_;
	}
	public CustomEntityMinecartChest(World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3, Config cfg_) {
	    super(paramWorld, paramDouble1, paramDouble2, paramDouble3);
	    config_ = cfg_;
	}
	
	@Override
	protected void h(){
        this.motX *= config_.get("minecart_drag").getDouble();
        this.motY *= 0.0D;
        this.motZ *= config_.get("minecart_drag").getDouble();
	}
	
}
