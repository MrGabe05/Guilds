package com.mrgabe.guilds.spigot.utils;

import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Locale;

public class Version {

    @Getter private static int version;

    public Version() {
        String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].toLowerCase(Locale.ROOT);
        version = Integer.parseInt(internalsName.split("_")[1]);
    }
}
