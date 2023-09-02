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
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/*
* MySQL Object class
*
* Class to support connections through MySQL.
* The database stores all the data of the Guilds and their members.
*/

public class MySQL {

    @Getter private static MySQL mySQL;

    private Connection connection;

    private final String TABLE_GUILDS = "guilds_data";
    private final String TABLE_MEMBERS = "guilds_members_data";

    /*
    * Initialize method to establish connection with the database.
    */
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

        mySQL = this;
    }

    /*
     * The tables are created in the database if they do not exist.
     */
    protected synchronized void setupTable() {
        try {
            this.execute("CREATE TABLE IF NOT EXISTS " + TABLE_GUILDS + " (id int, name VARCHAR(64), tag VARCHAR(6), leader VARCHAR(36), color int, settings LONGTEXT, points int, kills int, max_members int, created_at DATE, updated_at DATE, PRIMARY KEY (`id`));");
            this.execute("CREATE TABLE IF NOT EXISTS " + TABLE_MEMBERS + " (uuid VARCHAR(64), playerName VARCHAR(32), guild_id int, invited_by VARCHAR(36), rank id, joined_at DATE, updated_at DATE, PRIMARY KEY (`uuid`));");
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

    /*
    * Establishes the connection with the PoolSettings.
    * */
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

    /*
    * Get player data from MySQL database for the uuid.
    */
    public CompletableFuture<GuildPlayer> getPlayerByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE uuid='" + uuid.toString() + "';";

            GuildPlayer guildPlayer = new GuildPlayer(uuid);
            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    guildPlayer.setName(rs.getString("playerName"));
                    guildPlayer.setGuildId(rs.getInt("guild_id"));
                    guildPlayer.setRank(rs.getInt("rank"));
                    guildPlayer.setJoined(rs.getDate("joined_at"));
                    guildPlayer.setInvited((Utils.isValidString(rs.getString("invited_by")) ? UUID.fromString(rs.getString("invited_by")) : null));
                    return guildPlayer;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return guildPlayer;
        });
    }

    /*
     * Get player data from MySQL database for the name.
     */
    public CompletableFuture<GuildPlayer> getPlayerByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE playerName='" + name.toLowerCase() + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    GuildPlayer guildPlayer = new GuildPlayer(UUID.fromString(rs.getString("uuid")));
                    guildPlayer.setName(rs.getString(name));
                    guildPlayer.setGuildId(rs.getInt("guild_id"));
                    guildPlayer.setRank(rs.getInt("rank"));
                    guildPlayer.setJoined(rs.getDate("joined_at"));
                    guildPlayer.setInvited((Utils.isValidString(rs.getString("invited_by")) ? UUID.fromString(rs.getString("invited_by")) : null));

                    return guildPlayer;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return null;
        });
    }

    /*
    * Save all player data in the database.
    */
    public CompletableFuture<Void> savePlayer(GuildPlayer player) {
        return CompletableFuture.runAsync(() -> {
            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE uuid='" + player.getUuid().toString() + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    String UPDATE = "UPDATE " + TABLE_MEMBERS + " SET playerName='%s', guild_id='%s', invited_by='%s', rank='%s', joined_at='%s', updated_at='%s' WHERE uuid='%s';";
                    this.execute(UPDATE, player.getName(), player.getGuildId(), player.getInvited().toString(), player.getRank(), player.getJoined(), new Date(System.currentTimeMillis()), player.getUuid().toString());
                    return;
                }

                String INSERT = "INSERT INTO " + TABLE_MEMBERS + " (uuid, playerName, guild_id, invited_by, rank, joined_at, updated_at) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s');";
                this.execute(INSERT, player.getUuid(), player.getName(), player.getGuildId(), player.getInvited().toString(), player.getRank(), player.getJoined(), new Date(System.currentTimeMillis()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*
    * You get all registered players in a guild.
    * */
    public CompletableFuture<List<UUID>> getMembersFromGuild(Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> memberList = new ArrayList<>();

            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE guild_id='" + guild.getId() + "';";
            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null) {
                    while (rs.next()) memberList.add(UUID.fromString(rs.getString("uuid")));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return memberList;
        });
    }

    /*
    * Get guild data from MySQL database.
    * The guild information will be obtained by its ID.
    */
    public CompletableFuture<Guild> getGuildFromId(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String SELECT_DATA = "SELECT * FROM " + TABLE_GUILDS + " WHERE id='" + id + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    ObjectMapper objectMapper = new ObjectMapper();

                    Settings settings = objectMapper.readValue(rs.getString("settings"), Settings.class);

                    GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(UUID.fromString(rs.getString("leader"))).join();

                    Guild guild = new Guild(id, guildPlayer, settings);
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

    /*
    * Saves all guild data in the database.
    * */
    public CompletableFuture<Void> saveGuild(Guild guild) {
        return CompletableFuture.runAsync(() -> {
            ObjectMapper objectMapper = new ObjectMapper();

            String SELECT_DATA = "SELECT * FROM " + TABLE_GUILDS + " WHERE id='" + guild.getId() + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    String UPDATE = "UPDATE " + TABLE_GUILDS + " SET name='%s', tag='%s', leader='%s', color='%s', settings='%s', points='%s', kills='%s', max_members='%s', created_at='%s', updated_at='%s' WHERE id='%s';";
                    this.execute(UPDATE, guild.getName(), guild.getTag(), guild.getOwner().getUuid().toString(), guild.getColor(), objectMapper.writeValueAsString(guild.getSettings()), guild.getPoints(), guild.getKills(), guild.getMaxMembers(), guild.getDate(), new Date(System.currentTimeMillis()), guild.getId());
                    return;
                }

                String INSERT = "INSERT INTO " + TABLE_GUILDS + " (uuid, name, tag, leader, color, settings, points, kills, max_members, created_at, updated_at) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');";
                this.execute(INSERT, guild.getId(), guild.getName(), guild.getTag(), guild.getOwner().getUuid().toString(), guild.getColor(), objectMapper.writeValueAsString(guild.getSettings()), guild.getPoints(), guild.getKills(), guild.getMaxMembers(), guild.getDate(), new Date(System.currentTimeMillis()));
            } catch (SQLException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*
    * Close the connection to MySQL.
    * */
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
