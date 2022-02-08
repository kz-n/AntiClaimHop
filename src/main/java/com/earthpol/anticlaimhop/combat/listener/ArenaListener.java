package com.earthpol.anticlaimhop.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ArenaListener implements Listener {

    private Random random = new Random();

    @EventHandler
    public void onArmorDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();

        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(player.getLocation());
        if(townBlock == null || townBlock.getType() != TownBlockType.ARENA)
            return;

        if(!townBlock.hasTown())
            return;

        ItemStack item = event.getItem();
        if(!item.getType().toString().endsWith("HELMET") && !item.getType().toString().endsWith("CHESTPLATE") && !item.getType().toString().endsWith("LEGGINGS") && !item.getType().toString().endsWith("BOOTS"))
            return;

        if (10 < random.nextInt(100))
            return;

        event.setCancelled(true);
    }
}