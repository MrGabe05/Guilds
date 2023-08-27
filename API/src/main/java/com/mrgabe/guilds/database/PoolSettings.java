package com.mrgabe.guilds.database;

public class PoolSettings {

    public long MAX_LIFETIME, IDLE_TIMEOUT;

    public String CHARACTER_ENCODING, ENCODING;

    public boolean CACHE_PREP_STMTS, USE_UNICODE, USE_SSL;

    public int PREP_STMT_CACHE_SIZE, PREP_STMT_CACHE_SQL_LIMIT, MINIMUM_IDLE, MAXIMUM_POOL_SIZE;

    /*
    * Pool configuration for the MySQL database.
    * */
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
