package com.mrgabe.guilds.database;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CompletableFuture;

public class Redis {

    @Getter private static Redis redis;

    private final Jedis jedis;

    /*
    * System to support Redis.
    * */
    public Redis(String host, int port) {
        this.jedis = new Jedis(host, port);

        redis = this;
    }

    /*
    * This function will add data to the redis cache.
    * */
    public CompletableFuture<String> set(String key, String value) {
        return CompletableFuture.supplyAsync(() -> this.jedis.set(key, value));
    }

    /*
    * This function will get the data logged in redis.
    * */
    public CompletableFuture<String> get(String key) {
        return CompletableFuture.supplyAsync(() -> this.jedis.get(key));
    }

    public void close() {
        this.jedis.close();
    }
}