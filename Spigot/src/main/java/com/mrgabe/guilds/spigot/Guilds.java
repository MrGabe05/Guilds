package com.mrgabe.guilds.spigot;

import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.PoolSettings;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GManager;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.lang.Lang;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Main class to run the plugin on Spigot.
 * */
@Getter
public class Guilds extends JavaPlugin {

    @Getter private static Guilds instance;

    @Override
    public void onEnable() {
        instance = this;

        YamlConfig config = new YamlConfig(this, "Settings");
        new Redis(config.getString("Redis.Host"), config.getInt("Redis.Port"));

        this.loadMySQL(config);

        Lang.loadLangs();

        new GManager(this);
    }

    /*
     * onDisable method used to safely disconnect databases.
     * */
    @Override
    public void onDisable() {
        if(Redis.getRedis() != null) Redis.getRedis().close();
        if(MySQL.getMySQL() != null) MySQL.getMySQL().close();
    }

    /*
     * Load the MySQL configuration and initialize the class to establish connection.
     * */
    private void loadMySQL(YamlConfig config) {
        String host = config.getString("MySQL.Host");
        String port = config.getString("MySQL.Port");
        String database = config.getString("MySQL.Database");
        String username = config.getString("MySQL.Username");
        String password = config.getString("MySQL.Password");

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

        new MySQL(host, port, database, username, password, poolSettings);
    }
}