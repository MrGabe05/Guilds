package com.mrgabe.guilds.database;

import redis.clients.jedis.Jedis;

import java.util.concurrent.CompletableFuture;

public class Redis {

    private final Jedis jedis;

    public Redis(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    public CompletableFuture<String> set(String key, String value) {
        return CompletableFuture.supplyAsync(() -> this.jedis.set(key, value));
    }

    public CompletableFuture<String> get(String key) {
        return CompletableFuture.supplyAsync(() -> this.jedis.get(key));
    }

    public void close() {
        this.jedis.close();
    }
}