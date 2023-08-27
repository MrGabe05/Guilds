package com.mrgabe.guilds.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginLogger {

    private static final Logger logger = Logger.getLogger("Guilds");

    public static void info(String info) {
        logger.log(Level.INFO, info);
    }

    public static void error(String error) {
        logger.log(Level.SEVERE, error);
    }

    public static void debug(String debug) {
        logger.log(Level.WARNING, debug);
    }
}
