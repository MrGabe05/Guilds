package com.mrgabe.guilds.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Redis class for managing connections to a Redis server and performing cache operations.
 */
@Getter
public class Redis {

    @Getter private static Redis redis;

    private final String separator = "!;-;!";

    private final JedisPool jedisPool;

    /**
     * Initializes a connection to the Redis server with the specified host and port.
     *
     * @param host The Redis server host.
     * @param port The Redis server port.
     */
    public Redis(String host, int port) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        this.jedisPool = new JedisPool(poolConfig, host, port);

        redis = this;
    }

    /**
     * Asynchronously publishes a message to a Redis channel with the specified value.
     *
     * @param channel The Redis channel to which the message should be published.
     * @param value   The message content to publish.
     * @return A CompletableFuture representing the operation's completion.
     *         It will complete successfully when the message is successfully published,
     *         or exceptionally if an error occurs during the publishing process.
     */
    private CompletableFuture<Void> publish(String channel, String value) {
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = this.jedisPool.getResource()) {
                jedis.publish(channel, value);
            }
        });
    }

    /**
     * Publishes a message to the "guilds" Redis channel for a specific UUID.
     *
     * @param uuid    The UUID of the recipient.
     * @param message The message content to send.
     */
    public void sendMessage(UUID uuid, List<String> message) {
        try {
            // Serialize the message list into a JSON string and publish it to the "guilds" channel
            this.publish("guilds", "message" + separator + uuid.toString() + separator + new ObjectMapper().writeValueAsString(message));
        } catch (JsonProcessingException e) {
            // If a JSON processing exception occurs, throw a runtime exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Asynchronously deletes a key from the Redis cache if it exists.
     *
     * @param key The Redis key to delete.
     * @return A CompletableFuture representing the operation's completion.
     *         It will complete successfully when the key is successfully deleted (or if it doesn't exist),
     *         or exceptionally if an error occurs during the deletion process.
     */
    public CompletableFuture<Void> delete(String key) {
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = this.jedisPool.getResource()) {
                if (jedis.exists(key)) jedis.del(key);
            }
        });
    }

    /**
     * Stores data in the Redis cache with a specified key and value.
     * The data will be automatically deleted from the cache after 10 minutes of inactivity.
     *
     * @param key   The key under which the data will be stored.
     * @param value The data to be stored in the cache.
     * @return A CompletableFuture representing the completion of the cache operation.
     */
    public CompletableFuture<Void> set(String key, String value) {
        return CompletableFuture.runAsync(() -> {
            try(Jedis jedis = this.jedisPool.getResource()) {
                jedis.set(key, value);
                jedis.expire(key, 60 * 10); // Set an expiration time of 10 minutes (in seconds) for the key
            }
        });
    }

    /**
     * Retrieves data from the Redis cache based on the specified key.
     *
     * @param key The key for which to retrieve data from the cache.
     * @return A CompletableFuture containing the cached data as a String, or null if the key is not found.
     */
    public CompletableFuture<String> get(String key) {
        return CompletableFuture.supplyAsync(() -> {
            try(Jedis jedis = this.jedisPool.getResource()) {
                if (jedis.exists(key)) return jedis.get(key);
            }
            return null;
        });
    }

    /**
     * Closes the Redis connection pool when it's no longer needed.
     */
    public void close() {
        this.jedisPool.close();
    }
}
