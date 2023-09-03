package com.mrgabe.guilds.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A utility class for replacing placeholders in a string with corresponding values.
 */
public class Placeholders {

    // A map to store placeholders and their corresponding value suppliers
    private final Map<String, Supplier<String>> values = new HashMap<>();

    /**
     * Default constructor for the Placeholders class.
     */
    public Placeholders() {}

    /**
     * Sets a placeholder with a value supplier.
     *
     * @param placeholder The placeholder to be replaced in the string.
     * @param supplier    A supplier function that provides the value for the placeholder.
     * @param <K>         The type of the value supplied by the supplier.
     * @return The updated Placeholders object with the new placeholder and supplier.
     */
    public <K> Placeholders set(String placeholder, Supplier<K> supplier) {
        this.values.put(placeholder, () -> supplier.get().toString());
        return this;
    }

    /**
     * Sets a placeholder with a constant value.
     *
     * @param placeholder The placeholder to be replaced in the string.
     * @param value       The constant value to replace the placeholder with.
     * @param <K>         The type of the constant value.
     * @return The updated Placeholders object with the new placeholder and its constant value.
     */
    public <K> Placeholders set(String placeholder, K value) {
        return this.set(placeholder, () -> value);
    }

    /**
     * Parses a string by replacing placeholders with their corresponding values.
     *
     * @param s The input string containing placeholders to be replaced.
     * @return The parsed string with placeholders replaced by their values.
     */
    public String parse(String s) {
        String key;
        for (Iterator<String> iterator = this.values.keySet().iterator(); iterator.hasNext(); s = s.replace(key, this.values.get(key).get())) {
            key = iterator.next();
        }

        return s;
    }
}