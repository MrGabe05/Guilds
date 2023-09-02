package com.mrgabe.guilds.spigot.config;

import com.mrgabe.guilds.spigot.Guilds;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * A custom YamlConfiguration class for loading and managing plugin configuration files in Spigot.
 */
@Getter
public class YamlConfig extends YamlConfiguration {

    private final File file;
    private final String path;
    private final Guilds plugin;

    /**
     * Constructs a YamlConfig instance for a plugin with a given configuration file path.
     *
     * @param plugin The Guilds plugin instance.
     * @param path   The path to the YAML configuration file (excluding the ".yml" extension).
     */
    public YamlConfig(Guilds plugin, String path) {
        this.plugin = plugin;
        this.path = path + ".yml";
        this.file = new File(plugin.getDataFolder(), this.path);
        this.saveDefault();
        this.reload();
    }

    /**
     * Reloads the YAML configuration from the file.
     */
    public void reload() {
        try {
            super.load(this.file);
        } catch (Exception ignored) {
        }
    }

    /**
     * Saves the YAML configuration to the file.
     */
    public void save() {
        try {
            super.save(this.file);
        } catch (Exception ignored) {
        }
    }

    /**
     * Saves the default configuration file if it doesn't exist.
     */
    public void saveDefault() {
        try {
            if (!this.file.exists()) {
                if (plugin.getResource(this.path) != null) {
                    plugin.saveResource(this.path, false);
                } else {
                    this.file.createNewFile();
                }
            }
        } catch (Exception ignored) {
        }
    }
}