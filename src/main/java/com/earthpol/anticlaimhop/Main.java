package com.earthpol.anticlaimhop;

import com.earthpol.anticlaimhop.combat.CombatHandler;
import com.earthpol.anticlaimhop.combat.bossbar.BossBarTask;
import com.earthpol.anticlaimhop.combat.listener.CombatListener;
import com.earthpol.anticlaimhop.commands.CombatTagCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    private static Main instance;

    public static Main getInstance(){
        return instance;
    }

    private static Logger log = Bukkit.getLogger();

    public CombatHandler combatHandler;
    public CombatHandler getCombatHandler() {
        return combatHandler;
    }

    public static final String DISCORD = "https://earthpol.com/discord";

    @Override
    public void onEnable() {
        instance = this;
        log.info("§e======= §aEarthPol AntiClaimHop §e=======");
        log.info("§e= This plugin developed for EarthPol.");
        log.info("§e= Visit us at play.earthpol.com");
        log.info("§e= Join our discord at:" + DISCORD);
        setupListeners();
        setupCommands();
        runTasks();
    }

    private void setupListeners(){
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
    }

    private void setupCommands(){
        Objects.requireNonNull(getCommand("combattag")).setExecutor(new CombatTagCommand());
    }

    private void runTasks(){
        new BossBarTask().runTaskTimer(this, 10, 10);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
