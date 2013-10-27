package com.untamedears.humbug;

import org.bukkit.craftbukkit.v1_6_R3.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.minecraft.server.v1_6_R3.BlockMinecartTrackAbstract;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_6_R3.ItemMinecart;
import net.minecraft.server.v1_6_R3.ItemStack;
import net.minecraft.server.v1_6_R3.World;

public class CustomItemMinecart extends ItemMinecart{
	private Config config_;
	
	public CustomItemMinecart(int i, int j, Config cfg_) {
		super(i, j);
		config_ = cfg_;
	}
	
	public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, int i, int j, int k, int l, float f, float f1, float f2) {
	    int i1 = world.getTypeId(i, j, k);

	    if (BlockMinecartTrackAbstract.e_(i1)) {
	      if (!world.isStatic)
	      {
	        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(entityhuman, Action.RIGHT_CLICK_BLOCK, i, j, k, l, itemstack);

	        if (event.isCancelled()) {
	          return false;
	        }
	        EntityMinecartAbstract entityminecartabstract;
	        if(a==0){
	        	entityminecartabstract = new CustomEntityMinecartRideable(world, i + 0.5F, j + 0.5F, k + 0.5F, config_);
	        }
	        if(a<=1){
	        	entityminecartabstract = new CustomEntityMinecartChest(world, i + 0.5F, j + 0.5F, k + 0.5F, config_);
	        }else{
	        	entityminecartabstract = EntityMinecartAbstract.a(world, i + 0.5F, j + 0.5F, k + 0.5F, this.a);
	        }
	        
	        if (itemstack.hasName()) {
	          entityminecartabstract.a(itemstack.getName());
	        }

	        world.addEntity(entityminecartabstract);
	      }

	      itemstack.count -= 1;
	      return true;
	    }
	    return false;
	  }

}
