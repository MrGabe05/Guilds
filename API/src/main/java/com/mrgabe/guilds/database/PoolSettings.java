package com.mrgabe.guilds.database;

/**
 * Configuration class for storing settings related to the MySQL database connection pool.
 * This class holds various parameters that define the behavior of the database connection pool.
 */
public class PoolSettings {

    // Maximum lifetime of a database connection in milliseconds
    public long MAX_LIFETIME;

    // Timeout for an idle database connection in milliseconds
    public long IDLE_TIMEOUT;

    // Character encoding used for database communication
    public String CHARACTER_ENCODING;

    // Encoding format for database communication
    public String ENCODING;

    // Flag to enable caching of prepared statements
    public boolean CACHE_PREP_STMTS;

    // Flag to enable Unicode character support
    public boolean USE_UNICODE;

    // Flag to enable SSL encryption for the database connection
    public boolean USE_SSL;

    // Minimum number of idle database connections in the pool
    public int MINIMUM_IDLE;

    // Maximum number of database connections in the pool
    public int MAXIMUM_POOL_SIZE;

    // Size of the prepared statement cache
    public int PREP_STMT_CACHE_SIZE;

    // SQL statement cache limit
    public int PREP_STMT_CACHE_SQL_LIMIT;

    /**
     * Default constructor that initializes the pool settings with default values.
     * You can modify these settings as needed for your MySQL database configuration.
     */
    public PoolSettings() {
        MAX_LIFETIME = 180000L;
        IDLE_TIMEOUT = 60000L;
        CHARACTER_ENCODING = "utf8";
        ENCODING = "UTF-8";
        CACHE_PREP_STMTS = true;
        USE_UNICODE = true;
        USE_SSL = true;
        MINIMUM_IDLE = 1;
        MAXIMUM_POOL_SIZE = 8;
        PREP_STMT_CACHE_SIZE = 250;
        PREP_STMT_CACHE_SQL_LIMIT = 2048;
    }
}