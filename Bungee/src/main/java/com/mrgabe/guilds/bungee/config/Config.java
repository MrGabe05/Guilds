package com.mrgabe.guilds.bungee.config;

import com.mrgabe.guilds.bungee.Guilds;
import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * A utility class responsible for loading and providing access to the configuration settings of the Guilds BungeeCord plugin.
 */
@Getter
public class Config {

    private final File file;

    /**
     * Initializes the configuration file. If the file does not exist, it is created based on a default template.
     */
    public Config() {
        File configFile = new File(Guilds.getInstance().getDataFolder(), "Settings.yml");
        if (!configFile.exists()) {
            File loadFile = new File(Guilds.getInstance().getDataFolder(), "bungee-settings.yml");
            try {
                // Copy the default configuration template from the plugin resources
                try (InputStream is = Guilds.getInstance().getResourceAsStream("bungee-settings.yml")) {
                    Files.copy(is, loadFile.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error creating the config file. Please contact the developer (mrgabe5).", e);
            }

            // Rename the loaded file to the expected configuration file name
            loadFile.renameTo(configFile);
        }

        this.file = configFile;
    }

    /**
     * Retrieves the configuration settings using the BungeeCord API.
     *
     * @return The loaded configuration.
     */
    public Configuration getConfig() {
        Configuration configuration;
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);
        } catch (IOException e) {
            throw new RuntimeException("Error loading the config file. Please contact the developer (mrgabe5).", e);
        }
        return configuration;
    }
}