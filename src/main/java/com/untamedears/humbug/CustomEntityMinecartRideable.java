package com.untamedears.humbug;

import net.minecraft.server.v1_6_R3.EntityMinecartRideable;
import net.minecraft.server.v1_6_R3.World;

public class CustomEntityMinecartRideable extends EntityMinecartRideable{
	private Config config_;
	public CustomEntityMinecartRideable(World arg0, Config cfg_) {
		super(arg0);
		config_ = cfg_;
	}
	public CustomEntityMinecartRideable(World arg0, double arg1, double arg2, double arg3, Config cfg_) {
		super(arg0, arg1, arg2, arg3);
		config_ = cfg_;
	}
	protected void h(){
		if(this.passenger!=null){
		    this.motX *= config_.get("minecart_drag").getDouble();
		    this.motY *= 0.0D;
		    this.motZ *= config_.get("minecart_drag").getDouble();
		}
		else
        {
            this.motX *= 0.9599999785423279D;
            this.motY *= 0.0D;
            this.motZ *= 0.9599999785423279D;
        }
	}

}
