package com.untamedears.humbug;

import java.lang.reflect.*;
import java.util.Map;
import com.untamedears.humbug.Humbug;
import net.minecraft.server.v1_6_R2.Block;
import net.minecraft.server.v1_6_R2.Item;
import net.minecraft.server.v1_6_R2.TileEntity;

public class CustomNMSEnderChest {
  public static void setFinalStatic(
      Field field, Object newValue) throws Exception {
    // Remove private
    field.setAccessible(true);
    // Remove final
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    // Update value
    field.set(null, newValue);
  }

  @SuppressWarnings("unchecked")
  public static void InstallCustomEnderChest() {
    try {
      Block.byId[Block.ENDER_CHEST.id] = Block.CHEST;
      setFinalStatic(
          Block.class.getField("ENDER_CHEST"), Block.CHEST);

      Field aField = TileEntity.class.getDeclaredField("a");
      aField.setAccessible(true);
      Map aMap = (Map)aField.get(null);

      Field bField = TileEntity.class.getDeclaredField("b");
      bField.setAccessible(true);
      Map bMap = (Map)bField.get(null);

      Object tileEntityChest = aMap.get("Chest");
      Object tileEntityEnderChest = aMap.get("EnderChest");
      aMap.put("EnderChest", tileEntityChest);
      bMap.remove(tileEntityEnderChest);
    } catch (Exception ex) {
      Humbug.severe("Failed to install custom Ender Chest: " + ex);
    }
    if (Block.ENDER_CHEST != Block.CHEST) {
      Humbug.severe("Failed to install custom Ender Chest");
    }

  }
}

