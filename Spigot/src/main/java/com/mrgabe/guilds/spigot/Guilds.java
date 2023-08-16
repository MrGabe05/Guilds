package com.mrgabe.guilds.spigot;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Guilds extends JavaPlugin {

    @Getter private static Guilds instance;

    @Override
    public void onEnable() {
        instance = this;
    }
}