package com.mrgabe.guilds.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.Settings;
import com.mrgabe.guilds.utils.PluginLogger;
import com.mrgabe.guilds.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQL {

    private Connection connection;

    public MySQL(String host, String port, String database, String username, String password, PoolSettings poolSettings) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            this.setConnectionArguments(url, username, password, poolSettings);
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                PluginLogger.error("Invalid database arguments! Please check your configuration!");
                PluginLogger.error("If this error persists, please report it to the developer!");

                throw new IllegalArgumentException(e);
            }
            if (e instanceof HikariPool.PoolInitializationException) {
                PluginLogger.error("Can't initialize database connection! Please check your configuration!");
                PluginLogger.error("If this error persists, please report it to the developer!");
                throw new HikariPool.PoolInitializationException(e);
            }
            PluginLogger.error("Can't use the Hikari Connection Pool! Please, report this error to the developer!");
            throw e;
        }

        this.setupTable();
    }

    protected synchronized void setupTable() {
        try {
            this.execute("CREATE TABLE IF NOT EXISTS guilds_data (id int, name VARCHAR(64), tag VARCHAR(6), leader VARCHAR(36), color int, settings LONGTEXT, points int, kills int, max_members int, created_at DATE, updated_at DATE, PRIMARY KEY (`id`));");
            this.execute("CREATE TABLE IF NOT EXISTS guilds_members_data (guild_id int, uuid VARCHAR(36), invited_by VARCHAR(36), rank id, joined_at DATE, updated_at DATE, PRIMARY KEY (`id`));");
        } catch (SQLException e) {
            PluginLogger.error("Error inserting columns! Please check your configuration!");
            PluginLogger.error("If this error persists, please report it to the developer!");

            e.printStackTrace();
        }
    }

    protected void execute(String sql, Object... replacements) throws SQLException {
        Connection connection = this.connection;
        try(PreparedStatement statement = connection.prepareStatement(String.format(sql, replacements))) {
            statement.execute();
        }
    }

    protected synchronized void setConnectionArguments(String url, String username, String password, PoolSettings settings) throws RuntimeException {
        try(HikariDataSource ds = new HikariDataSource()) {
            ds.setPoolName("Guilds MySQL");
            ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setJdbcUrl(url);
            ds.addDataSourceProperty("cachePrepStmts", settings.CACHE_PREP_STMTS);
            ds.addDataSourceProperty("prepStmtCacheSize", settings.PREP_STMT_CACHE_SIZE);
            ds.addDataSourceProperty("prepStmtCacheSqlLimit", settings.PREP_STMT_CACHE_SQL_LIMIT);
            ds.addDataSourceProperty("characterEncoding", settings.CHARACTER_ENCODING);
            ds.addDataSourceProperty("encoding", settings.ENCODING);
            ds.addDataSourceProperty("useUnicode", settings.USE_UNICODE);
            ds.addDataSourceProperty("useSSL", settings.USE_SSL);
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setMaxLifetime(settings.MAX_LIFETIME);
            ds.setIdleTimeout(settings.IDLE_TIMEOUT);
            ds.setMinimumIdle(settings.MINIMUM_IDLE);
            ds.setMaximumPoolSize(settings.MAXIMUM_POOL_SIZE);

            this.connection = ds.getConnection();
        } catch (SQLException e) {
            PluginLogger.error("Error on setting connection!");
        }

        PluginLogger.info("Connection arguments loaded, Hikari ConnectionPool ready!");
    }

    public CompletableFuture<GuildPlayer> getPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String SELECT_DATA = "SELECT * FROM guilds_members_data WHERE uuid='" + uuid.toString() + "';";

            GuildPlayer guildPlayer = new GuildPlayer(uuid);
            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    guildPlayer.setGangId(rs.getInt("gang_id"));
                    guildPlayer.setRank(rs.getInt("rank"));
                    guildPlayer.setJoined((rs.getDate("joined_at") != null ? rs.getDate("joined_at") : null));
                    guildPlayer.setInvited((Utils.isValidString(rs.getString("invited_by")) ? UUID.fromString(rs.getString("invited_by")) : null));
                    return guildPlayer;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return guildPlayer;
        });
    }

    public CompletableFuture<Void> savePlayer(GuildPlayer player) {
        return CompletableFuture.runAsync(() -> {

        });
    }

    public CompletableFuture<Guild> getGuildFromID(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String SELECT_DATA = "SELECT * FROM guilds_data WHERE id='" + id + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    ObjectMapper objectMapper = new ObjectMapper();

                    Settings settings = objectMapper.readValue(rs.getString("settings"), Settings.class);

                    Guild guild = new Guild(id, UUID.fromString(rs.getString("leader")), settings);
                    guild.setName(rs.getString("name"));
                    guild.setTag(rs.getString("tag"));
                    guild.setColor(rs.getInt("color"));
                    guild.setPoints(rs.getInt("points"));
                    guild.setKills(rs.getInt("kills"));
                    guild.setDate(rs.getDate("created_at"));
                    return guild;
                }
            } catch (SQLException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
}
