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

/**
* MySQL Object class
* Class to support connections through MySQL.
* The database stores all the data of the Guilds and their members.
*/
public class MySQL {

    @Getter private static MySQL mySQL;

    private Connection connection;

    private final String TABLE_GUILDS = "guilds_data";
    private final String TABLE_MEMBERS = "guilds_members_data";

    /**
     * Initializes the MySQL class by establishing a database connection.
     *
     * @param host          MySQL server host address.
     * @param port          MySQL server port.
     * @param database      Name of the MySQL database.
     * @param username      MySQL username for authentication.
     * @param password      MySQL password for authentication.
     * @param poolSettings  PoolSettings object containing connection pool configuration.
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

    /**
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

    /**
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

    /**
     * Retrieves a GuildPlayer object by UUID from the MySQL database.
     *
     * @param uuid The UUID of the player to retrieve.
     * @return A CompletableFuture that, when completed, returns the GuildPlayer object if found in the database.
     *         Returns a GuildPlayer object with default values if not found.
     * @throws RuntimeException If a database error occurs while retrieving the player's data.
     */
    public CompletableFuture<GuildPlayer> getPlayerByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // SQL query to select player data by UUID
            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE uuid='" + uuid.toString() + "';";

            // Initialize a GuildPlayer object with the given UUID
            GuildPlayer guildPlayer = new GuildPlayer(uuid);
            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    // Populate GuildPlayer attributes with data from the database
                    guildPlayer.setName(rs.getString("playerName"));
                    guildPlayer.setGuildId(rs.getInt("guild_id"));
                    guildPlayer.setRank(rs.getInt("rank"));
                    guildPlayer.setJoined(rs.getDate("joined_at"));

                    // Check if the "invited_by" field contains a valid UUID, and set it if so
                    guildPlayer.setInvited((Utils.isValidString(rs.getString("invited_by")) ? UUID.fromString(rs.getString("invited_by")) : null));

                    return guildPlayer; // Return the populated GuildPlayer object
                }
            } catch (SQLException e) {
                // Throw a RuntimeException if a database error occurs
                throw new RuntimeException(e);
            }

            return guildPlayer; // Return the GuildPlayer object with default values if not found
        });
    }

    /**
     * Retrieves a UUID object by player name (case-insensitive) from the MySQL database.
     *
     * @param name The name of the player to retrieve.
     * @return A CompletableFuture that, when completed, returns the UUID object if found in the database.
     *         Returns null if the player with the specified name is not found.
     * @throws RuntimeException If a database error occurs while retrieving the player's data.
     */
    public CompletableFuture<UUID> getPlayerUuid(String name) {
        return CompletableFuture.supplyAsync(() -> {
            // SQL query to select player data by player name (case-insensitive)
            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE playerName='" + name.toLowerCase() + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) return UUID.fromString(rs.getString("uuid")); // Return the populated GuildPlayer object
            } catch (SQLException e) {
                // Throw a RuntimeException if a database error occurs
                throw new RuntimeException(e);
            }

            return null; // Return null if the player with the specified name is not found
        });
    }

    /**
     * Saves or updates player data in the MySQL database.
     *
     * @param player The GuildPlayer object containing the player's data to be saved or updated.
     * @return A CompletableFuture representing the asynchronous completion of the database operation.
     * @throws RuntimeException If a database error occurs while saving or updating the player's data.
     */
    public CompletableFuture<Void> savePlayer(GuildPlayer player) {
        return CompletableFuture.runAsync(() -> {
            // SQL query to check if the player already exists in the database
            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE uuid='" + player.getUuid().toString() + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    // If the player exists, update their data in the database
                    String UPDATE = "UPDATE " + TABLE_MEMBERS + " SET playerName='%s', guild_id='%s', invited_by='%s', rank='%s', joined_at='%s', updated_at='%s' WHERE uuid='%s';";
                    this.execute(UPDATE, player.getName(), player.getGuildId(), player.getInvited().toString(), player.getRank(), player.getJoined(), new Date(System.currentTimeMillis()), player.getUuid().toString());
                    return; // Return without inserting a new record
                }

                // If the player does not exist, insert their data into the database
                String INSERT = "INSERT INTO " + TABLE_MEMBERS + " (uuid, playerName, guild_id, invited_by, rank, joined_at, updated_at) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s');";
                this.execute(INSERT, player.getUuid(), player.getName(), player.getGuildId(), player.getInvited().toString(), player.getRank(), player.getJoined(), new Date(System.currentTimeMillis()));
            } catch (SQLException e) {
                // Throw a RuntimeException if a database error occurs
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Retrieves a list of UUIDs representing the members of a guild from the MySQL database.
     *
     * @param guild The Guild object for which to fetch the member UUIDs.
     * @return A CompletableFuture containing a List of UUIDs representing the members of the guild.
     * @throws RuntimeException If a database error occurs while fetching the member data.
     */
    public CompletableFuture<List<UUID>> getMembersFromGuild(Guild guild) {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> memberList = new ArrayList<>();

            // SQL query to retrieve member UUIDs belonging to the specified guild
            String SELECT_DATA = "SELECT * FROM " + TABLE_MEMBERS + " WHERE guild_id='" + guild.getId() + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null) {
                    while (rs.next()) memberList.add(UUID.fromString(rs.getString("uuid")));
                }
            } catch (SQLException e) {
                // Throw a RuntimeException if a database error occurs
                throw new RuntimeException(e);
            }
            return memberList;
        });
    }

    /**
     * Returns the total number of guild data entries in the database.
     *
     * @return The total number of guild data entries.
     */
    public int getGuildDataSize() {
        int size = 0;

        String SELECT_DATA = "SELECT * FROM " + TABLE_GUILDS + ";";
        try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
            ResultSet rs = statement.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    size++;
                }
            }
        } catch (SQLException e) {
            // Throws a RuntimeException if a database error occurs
            throw new RuntimeException(e);
        }

        return size;
    }

    /**
     * Retrieves a list of all guilds from the database.
     *
     * @return A CompletableFuture containing a List of Guild objects.
     */
    public CompletableFuture<List<Guild>> getGuildList() {
        return CompletableFuture.supplyAsync(() -> {
            List<Guild> guilds = new ArrayList<>();

            String SELECT_DATA = "SELECT * FROM " + TABLE_GUILDS + ";";
            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        guilds.add(getGuildFromId(rs.getInt("id")).join());
                    }
                }
            } catch (SQLException e) {
                // Throws a RuntimeException if a database error occurs
                throw new RuntimeException(e);
            }
            return guilds;
        });
    }

    /**
     * Retrieves a Guild object from the MySQL database based on its unique ID.
     *
     * @param id The unique ID of the guild to fetch from the database.
     * @return A CompletableFuture containing the Guild object with the specified ID, or null if not found.
     * @throws RuntimeException If a database error occurs while fetching the guild data.
     */
    public CompletableFuture<Guild> getGuildFromId(int id) {
        return CompletableFuture.supplyAsync(() -> {
            // SQL query to retrieve guild data based on the provided ID
            String SELECT_DATA = "SELECT * FROM " + TABLE_GUILDS + " WHERE id='" + id + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    // Deserialize settings from a JSON string in the database
                    Settings settings = new ObjectMapper().readValue(rs.getString("settings"), Settings.class);

                    // Fetch the guild leader's data and block until it completes
                    GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(UUID.fromString(rs.getString("leader"))).join();

                    // Create a Guild object with the retrieved data
                    Guild guild = new Guild(id, settings);
                    guild.setOwner(guildPlayer);
                    guild.setName(rs.getString("name"));
                    guild.setTag(rs.getString("tag"));
                    guild.setColor(rs.getInt("color"));
                    guild.setPoints(rs.getInt("points"));
                    guild.setKills(rs.getInt("kills"));
                    guild.setDate(rs.getDate("created_at"));
                    return guild;
                }
            } catch (SQLException | JsonProcessingException e) {
                // Throw a RuntimeException if a database error occurs
                throw new RuntimeException(e);
            }
            return null; // Return null if the guild is not found in the database
        });
    }

    /**
     * Saves a Guild object's data to the MySQL database.
     *
     * @param guild The Guild object to be saved in the database.
     * @return A CompletableFuture representing the completion of the database operation.
     * @throws RuntimeException If a database error occurs while saving the guild data.
     */
    public CompletableFuture<Void> saveGuild(Guild guild) {
        return CompletableFuture.runAsync(() -> {
            ObjectMapper objectMapper = new ObjectMapper();

            // SQL query to check if the guild with the specified ID already exists in the database
            String SELECT_DATA = "SELECT * FROM " + TABLE_GUILDS + " WHERE id='" + guild.getId() + "';";

            try (PreparedStatement statement = connection.prepareStatement(SELECT_DATA)) {
                ResultSet rs = statement.executeQuery();
                if (rs != null && rs.next()) {
                    // If the guild exists, update its data in the database
                    String UPDATE = "UPDATE " + TABLE_GUILDS + " SET name='%s', tag='%s', leader='%s', color='%s', settings='%s', points='%s', kills='%s', max_members='%s', created_at='%s', updated_at='%s' WHERE id='%s';";
                    this.execute(UPDATE, guild.getName(), guild.getTag(), guild.getOwner().getUuid().toString(), guild.getColor(), objectMapper.writeValueAsString(guild.getSettings()), guild.getPoints(), guild.getKills(), guild.getMaxMembers(), guild.getDate(), new Date(System.currentTimeMillis()), guild.getId());
                    return;
                }

                // If the guild doesn't exist, insert a new record into the database
                String INSERT = "INSERT INTO " + TABLE_GUILDS + " (id, name, tag, leader, color, settings, points, kills, max_members, created_at, updated_at) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');";
                this.execute(INSERT, guild.getId(), guild.getName(), guild.getTag(), guild.getOwner().getUuid().toString(), guild.getColor(), objectMapper.writeValueAsString(guild.getSettings()), guild.getPoints(), guild.getKills(), guild.getMaxMembers(), guild.getDate(), new Date(System.currentTimeMillis()));
            } catch (SQLException | JsonProcessingException e) {
                // Throw a RuntimeException if a database error occurs
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Asynchronously removes a guild with the specified ID from the database.
     *
     * @param id The ID of the guild to remove.
     * @return A CompletableFuture representing the operation's completion.
     *         It will complete successfully when the guild is successfully removed,
     *         or exceptionally if an error occurs during the removal process.
     */
    public CompletableFuture<Void> removeGuild(int id) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Execute a SQL DELETE statement to remove the guild with the specified ID
                this.execute("DELETE FROM " + TABLE_GUILDS + " WHERE id='%s';", id);
            } catch (SQLException e) {
                // If an SQL exception occurs during the removal process, throw a runtime exception
                throw new RuntimeException(e);
            }
        });
    }

    /**
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
