package com.mrgabe.guilds.utils;

public class Utils {

    /**
     * Checks if a string is valid, meaning it is not null, not empty, and not just whitespace.
     *
     * @param s The string to check.
     * @return true if the string is valid, false otherwise.
     */
    public static boolean isValidString(String s) {
        return s != null && !s.isEmpty() && !s.trim().isEmpty();
    }

    /**
     * Parses a string into a boolean value, considering common affirmative strings.
     *
     * @param s The string to parse.
     * @return true if the string is an affirmative indicator, false otherwise.
     */
    public static boolean parseBoolean(String s) {
        return s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("confirm") || s.equalsIgnoreCase("confirmed") || s.equalsIgnoreCase("yep");
    }

    /**
     * Replaces color codes in a string using '&' with Minecraft formatting codes ('§').
     *
     * @param s The string containing color codes.
     * @return The string with color codes replaced by Minecraft formatting codes.
     */
    public static String color(String s) {
        return s.replaceAll("&", "§");
    }
}
