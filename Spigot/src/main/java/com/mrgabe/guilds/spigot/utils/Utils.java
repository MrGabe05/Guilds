package com.mrgabe.guilds.spigot.utils;

import org.bukkit.entity.Player;

public class Utils {

    public static String color(String s) {
        return s.replaceAll("&", "§");
    }

    public static String getLocale(Player player) {
        return player.getLocale();
    }
}
