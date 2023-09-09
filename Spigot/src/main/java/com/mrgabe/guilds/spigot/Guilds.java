package com.mrgabe.guilds.spigot;

import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.menus.impl.ConfirmMenu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * Main class for running the Guilds plugin on Spigot.
 */
@Getter
public class Guilds extends JavaPlugin {

    @Getter private static Guilds instance;

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, loads configuration, establishes database connections, and sets up the command manager.
     */
    @Override
    public void onEnable() {
        instance = this;

        // Load configuration from Yaml file
        YamlConfig config = new YamlConfig(this, "Settings");

        // Initialize Redis connection
        this.loadRedis(config);
    }

    /**
     * Load Redis with the provided configuration and set up message subscription.
     *
     * @param config The BungeeCord configuration containing Redis connection details.
     */
    public void loadRedis(FileConfiguration config) {
        // Initialize Redis connection using the configuration.
        new Redis(config.getString("Redis.Host"), config.getInt("Redis.Port"));

        // Define a JedisPubSub instance for message handling.
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if(channel.endsWith("confirm")) {
                    String[] split = message.split(":");

                    Player player = Bukkit.getPlayer(UUID.fromString(split[1]));
                    if(player != null) {
                        new ConfirmMenu(response -> {
                            if (response) {
                                Redis.getRedis().publish(channel.split("-")[0], message);
                            }
                        }).open(player);
                    }
                }
            }
        };

        // Subscribe to the "guilds" and "chat" channels for message reception.
        try (Jedis jedis = Redis.getRedis().getJedisPool().getResource()) {
            jedis.subscribe(jedisPubSub, "disband-confirm", "disband", "transfer-confirm", "transfer");
        }
    }

    /**
     * Called when the plugin is disabled.
     * Safely disconnects from databases if they were connected.
     */
    @Override
    public void onDisable() {
        // Close the Redis connection if it's open
        if (Redis.getRedis() != null) {
            Redis.getRedis().close();
        }
    }
}