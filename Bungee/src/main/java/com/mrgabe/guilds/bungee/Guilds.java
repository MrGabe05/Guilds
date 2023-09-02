package com.mrgabe.guilds.bungee;

import com.mrgabe.guilds.bungee.config.Config;
import com.mrgabe.guilds.bungee.listeners.Listeners;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.PoolSettings;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.utils.Utils;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * Main class for running the Guilds plugin on BungeeCord.
 */
@Getter
public class Guilds extends Plugin {

    @Getter private static Guilds instance;

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, loads configuration, establishes database connections, and registers listeners.
     */
    @Override
    public void onEnable() {
        instance = this;

        // Load configuration using the Config class
        Configuration config = new Config().getConfig();

        // Initialize Redis connection
        this.loadRedis(config);

        // Load MySQL configuration and establish a connection
        this.loadMySQL(config);

        // Register event listeners
        this.getProxy().getPluginManager().registerListener(this, new Listeners());
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

        // Close the MySQL connection if it's open
        if (MySQL.getMySQL() != null) {
            MySQL.getMySQL().close();
        }
    }

    /**
     * Load Redis with the provided configuration and set up message subscription.
     *
     * @param config The BungeeCord configuration containing Redis connection details.
     */
    public void loadRedis(Configuration config) {
        // Initialize Redis connection using the configuration.
        new Redis(config.getString("Redis.Host"), config.getInt("Redis.Port"));

        // Define a JedisPubSub instance for message handling.
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                switch (channel.toLowerCase()) {
                    case "guilds": {
                        if (message.startsWith("message")) {
                            String[] split = message.split(Redis.getRedis().getSeparator());

                            // Get the player using their UUID.
                            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(split[0]));

                            // Check if the player is connected before sending a message.
                            if (player != null && player.isConnected()) {
                                player.sendMessage(player.getUniqueId(), new TextComponent(Utils.color(split[1])));
                            }
                        }
                        break;
                    }
                    case "chat": {
                        String[] split = message.split(Redis.getRedis().getSeparator());

                        // Get the player using their UUID.
                        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(split[0]));

                        // Check if the player is connected before sending a message.
                        if (player != null && player.isConnected()) {
                            player.sendMessage(player.getUniqueId(), new TextComponent(Utils.color(split[1])));
                        }
                        break;
                    }
                }
            }
        };

        // Subscribe to the "guilds" and "chat" channels for message reception.
        try (Jedis jedis = Redis.getRedis().getJedisPool().getResource()) {
            jedis.subscribe(jedisPubSub, "guilds", "chat");
        }
    }

    /**
     * Loads MySQL configuration and establishes a connection.
     *
     * @param config The Configuration object containing MySQL configuration settings.
     */
    private void loadMySQL(Configuration config) {
        String host = config.getString("MySQL.Host");
        String port = config.getString("MySQL.Port");
        String database = config.getString("MySQL.Database");
        String username = config.getString("MySQL.Username");
        String password = config.getString("MySQL.Password");

        // Configure MySQL connection pool settings
        PoolSettings poolSettings = new PoolSettings();
        poolSettings.CACHE_PREP_STMTS = config.getBoolean("MySQL.PoolSettings.CachePrepStmts");
        poolSettings.PREP_STMT_CACHE_SIZE = config.getInt("MySQL.PoolSettings.PrepStmtCacheSize");
        poolSettings.PREP_STMT_CACHE_SQL_LIMIT = config.getInt("MySQL.PoolSettings.PrepStmtCacheSqlLimit");
        poolSettings.CHARACTER_ENCODING = config.getString("MySQL.PoolSettings.CharacterEncoding");
        poolSettings.ENCODING = config.getString("MySQL.PoolSettings.Encoding");
        poolSettings.USE_UNICODE = config.getBoolean("MySQL.PoolSettings.UseUnicode");
        poolSettings.MAX_LIFETIME = config.getLong("MySQL.PoolSettings.MaxLifetime");
        poolSettings.IDLE_TIMEOUT = config.getLong("MySQL.PoolSettings.IdleTimeout");
        poolSettings.MINIMUM_IDLE = config.getInt("MySQL.PoolSettings.MinimumIdle");
        poolSettings.MAXIMUM_POOL_SIZE = config.getInt("MySQL.PoolSettings.MaximumPoolSize");
        poolSettings.USE_SSL = config.getBoolean("MySQL.PoolSettings.UseSSL");

        // Initialize the MySQL connection
        new MySQL(host, port, database, username, password, poolSettings);
    }
}