package com.earthpol.anticlaimhop.combat.listener;

import com.earthpol.anticlaimhop.combat.CombatHandler;
import com.earthpol.anticlaimhop.combat.bossbar.BossBarTask;
import com.google.common.collect.ImmutableSet;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatListener implements Listener {

        private Set<UUID> deathsForLoggingOut = new HashSet<>();

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onDamage(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player))
                return;

            Player damagee = (Player) event.getEntity();
            Player damager;

            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                if (shooter == null || !(shooter instanceof Player))
                    return;

                damager = (Player) shooter;
            } else {
                return;
            }

            if (damager.equals(damagee))
                return;

            CombatHandler.applyTag(damagee);
            CombatHandler.applyTag(damager);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onCobweb(BlockPlaceEvent event) {
            if (event.getBlockPlaced().getType() != Material.COBWEB)
                return;

            if (!CombatHandler.isTagged(event.getPlayer()))
                return;

            event.setCancelled(true);

            event.getPlayer().sendMessage(ChatColor.RED + "You can't place cobwebs while being in combat.");
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();

            BossBarTask.remove(player);

            if (!CombatHandler.isTagged(player))
                return;

            CombatHandler.removeTag(player);

            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(player.getLocation());
            if(townBlock != null && townBlock.getType() == TownBlockType.ARENA && townBlock.hasTown())
                return;

            deathsForLoggingOut.add(player.getUniqueId());
            player.setHealth(0.0);
        }

        @EventHandler
        public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();

            if (deathsForLoggingOut.contains(player.getUniqueId())) {
                deathsForLoggingOut.remove(player.getUniqueId());
                event.deathMessage(Component.text(player.getName() + " was killed for logging out in combat."));
            }

            if (!CombatHandler.isTagged(player))
                return;

            CombatHandler.removeTag(player);
        }

        // Lowercase
        private static final Set<String> WHITELISTED_COMMANDS = ImmutableSet.of("tc", "nc", "g", "ally", "msg", "r", "reply", "tell", "pm", "mod", "admin", "combattag", "lc");

        @EventHandler
        public void onPreProcessCommand(PlayerCommandPreprocessEvent event) {
            Player player = event.getPlayer();
            if (!CombatHandler.isTagged(player) || player.hasPermission("earthpol.combattag.bypass"))
                return;

            String message = event.getMessage();
            message = message.replaceFirst("/", "");

            for (String value : WHITELISTED_COMMANDS) {
                if (message.equalsIgnoreCase(value) || message.toLowerCase().startsWith(value + " "))
                    return;
            }

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't use that command while being in combat.");
        }

        // Prevent claim hopping
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPvP(TownyPlayerDamagePlayerEvent event) {
            if (!event.isCancelled())
                return;

            TownyWorld world = TownyAPI.getInstance().getTownyWorld(event.getVictimPlayer().getWorld().getName());
            Player attacker = event.getAttackingPlayer();
            Player victim = event.getVictimPlayer();

            if (!world.isFriendlyFireEnabled() && CombatUtil.isAlly(attacker.getName(), victim.getName()))
                return;

            if (!CombatHandler.isTagged(victim))
                return;

            event.setCancelled(false);
        }

        @EventHandler
        public void onOpen(InventoryOpenEvent event) {
            if (event.getInventory().getType() != InventoryType.ENDER_CHEST)
                return;

            if (!(event.getPlayer() instanceof Player))
                return;

            Player player = (Player) event.getPlayer();

            if (!CombatHandler.isTagged(player))
                return;

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't use ender chest while being in combat.");
        }

        @EventHandler
        public void onRiptide(PlayerMoveEvent event) {
            Player player = event.getPlayer();

            if (!CombatHandler.isTagged(player))
                return;

            if (!player.isRiptiding())
                return;

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "The riptide enchantment is disabled while being in combat.");
        }
}
