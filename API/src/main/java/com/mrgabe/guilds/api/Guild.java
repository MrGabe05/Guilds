package com.mrgabe.guilds.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.Redis;
import lombok.Data;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/*
* Guild object class
* Class where all the information of the guild is.
* */

@Data
public class Guild {

    private final int id;

    private final UUID owner;

    private final Settings settings;

    private String name, tag;

    private int kills, points, color;

    private Date date = new Date(System.currentTimeMillis());

    public CompletableFuture<List<UUID>> fetchMembers() {
        return CompletableFuture.supplyAsync(() -> MySQL.getMySQL().getMembersFromGuild(this).join());
    }

    public static CompletableFuture<Guild> ofMember(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            GuildPlayer guildPlayer = GuildPlayer.of(uuid).join();

            if(guildPlayer.getGangId() > 0) {
                return ofID(guildPlayer.getGangId()).join();
            }
            return null;
        });
    }

    public static CompletableFuture<Guild> ofID(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String guildData = Redis.getRedis().get(String.valueOf(id)).join();
            ObjectMapper objectMapper = new ObjectMapper();

            Guild guild = null;

            try {
                guild = (guildData != null ? objectMapper.readValue(guildData, Guild.class) : MySQL.getMySQL().getGuildFromID(id).join());

                Redis.getRedis().set(String.valueOf(id), objectMapper.writeValueAsString(guild));
            } catch (JsonProcessingException ignored) {}

            return guild;
        });
    }
}
