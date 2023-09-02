package com.mrgabe.guilds.database;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.CompletableFuture;

public class Redis {

    @Getter private static Redis redis;

    private final JedisPool jedisPool;

    /*
    * Initialize method to establish connection with the Redis.
    * */
    public Redis(String host, int port) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = new JedisPool(poolConfig, host, port);

        redis = this;
    }

    /*
    * This function will add data to the redis cache.
    * It will delete the cache data after 10 minutes of inactivity.
    * */
    public CompletableFuture<Void> set(String key, String value) {
        return CompletableFuture.runAsync(() -> {
            this.jedisPool.getResource().set(key, value);
            this.jedisPool.getResource().expire(key, (60 * 10));
        });
    }

    /*
    * This function will get the data logged in redis.
    * */
    public CompletableFuture<String> get(String key) {
        return CompletableFuture.supplyAsync(() -> {
            if(this.jedisPool.getResource().exists(key))
                this.jedisPool.getResource().get(key);

            return null;
        });
    }

    public void close() {
        this.jedisPool.close();
    }
}