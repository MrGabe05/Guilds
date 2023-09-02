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

/*
* Class in charge of loading the configuration
* */
@Getter
public class Config {

    private final File file;

    /*
    * Initializer which will create a new file with all the information
    * of the file that contains the plugin in such a case that it does not exist.
    * */
    public Config() {
        File configFile = new File(Guilds.getInstance().getDataFolder(), "Settings.yml");
        if (!configFile.exists()) {
            File loadFile = new File(Guilds.getInstance().getDataFolder(), "bungee-settings.yml");
            try {
                try (InputStream is = Guilds.getInstance().getResourceAsStream("bungee-settings.yml")) {
                    Files.copy(is, loadFile.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error on create file config, please contact with Developer: mrgabe5", e);
            }

            loadFile.renameTo(configFile);
        }

        this.file = configFile;
    }

    /*
    * You get the configuration using the BungeeCord API.
    * */
    public Configuration getConfig() {
        Configuration configuration;
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);
        } catch (IOException e) {
            throw new RuntimeException("Error on create file config, please contact with Developer: mrgabe5", e);
        }
        return configuration;
    }
}