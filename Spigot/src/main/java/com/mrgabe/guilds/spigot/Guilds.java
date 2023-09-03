package com.mrgabe.guilds.spigot;

import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.PoolSettings;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GManager;
import com.mrgabe.guilds.spigot.commands.admin.AdminCommands;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.lang.Lang;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

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
        new Redis(config.getString("Redis.Host"), config.getInt("Redis.Port"));

        // Load MySQL configuration and establish a connection
        this.loadMySQL(config);

        // Load language configurations
        Lang.loadLangs();

        // Initialize the command manager
        new GManager(this);

        this.getCommand("gadmin").setExecutor(new AdminCommands());
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
     * Loads MySQL configuration and establishes a connection.
     *
     * @param config The YamlConfig object containing MySQL configuration settings.
     */
    private void loadMySQL(YamlConfig config) {
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