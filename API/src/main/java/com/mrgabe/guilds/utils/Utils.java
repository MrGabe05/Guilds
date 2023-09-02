package com.mrgabe.guilds.utils;

public class Utils {

    /*
    * This method verifies that the String is valid.
    * */
    public static boolean isValidString(String s) {
        return s != null && !s.isEmpty() && !s.trim().isEmpty();
    }

    public static String color(String s) {
        return s.replaceAll("&", "§");
    }
}
