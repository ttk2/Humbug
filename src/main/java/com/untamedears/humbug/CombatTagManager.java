package com.untamedears.humbug;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;

class CombatTagManager {
    private CombatTagApi combatTagApi_;
    private boolean combatTagEnabled_ = false;

    public CombatTagManager() {
        if(Bukkit.getPluginManager().getPlugin("CombatTag") != null) {
            combatTagApi_ = new CombatTagApi((CombatTag)Bukkit.getPluginManager().getPlugin("CombatTag"));
            combatTagEnabled_ = true;
        }
    }

    public boolean tagPlayer(Player player) {
        if (combatTagEnabled_ && combatTagApi_ != null) {
            return combatTagApi_.tagPlayer(player);
        }
        return false;
    }

    public boolean tagPlayer(String player) {
        if (combatTagEnabled_ && combatTagApi_ != null) {
            combatTagApi_.tagPlayer(player);
            return true;
        }
        return false;
    }
}
