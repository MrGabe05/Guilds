package com.mrgabe.guilds.bungee;

import com.mrgabe.guilds.bungee.config.Config;
import com.mrgabe.guilds.bungee.listeners.Listeners;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.PoolSettings;
import com.mrgabe.guilds.database.Redis;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

/*
* Main class to run the plugin on BungeeCord.
* */
@Getter
public class Guilds extends Plugin {

    @Getter private static Guilds instance;

    @Override
    public void onEnable() {
        instance = this;

        Configuration config = new Config().getConfig();

        new Redis(config.getString("Redis.Host"), config.getInt("Redis.Port"));

        this.loadMySQL(config);

        this.getProxy().getPluginManager().registerListener(this, new Listeners());
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
    private void loadMySQL(Configuration config) {
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
