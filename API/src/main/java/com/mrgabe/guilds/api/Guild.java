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

    private final GuildPlayer owner;

    private final Settings settings;

    private String name, tag;

    private int kills, points, color, maxMembers;

    private Date date = new Date(System.currentTimeMillis());

    /*
    * All members that are in the guild will be obtained.
    * */
    public CompletableFuture<List<UUID>> fetchMembers() {
        return CompletableFuture.supplyAsync(() -> MySQL.getMySQL().getMembersFromGuild(this).join());
    }

    public CompletableFuture<Void> saveGuild() {
        return CompletableFuture.runAsync(() -> {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                Redis.getRedis().set(String.valueOf(this.id), objectMapper.writeValueAsString(this));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            MySQL.getMySQL().saveGuild(this);
        });
    }

    /*
    * The guild will be obtained by some member.
    * */
    public static CompletableFuture<Guild> getGuildByMember(Object key) {
        return CompletableFuture.supplyAsync(() -> {
            GuildPlayer guildPlayer = null;
            if(key instanceof UUID) {
                guildPlayer = GuildPlayer.getPlayerByUuid((UUID)key).join();
            } else if(key instanceof String) {
                guildPlayer = GuildPlayer.getPlayerByName((String)key).join();
            }

            if(guildPlayer != null && guildPlayer.getGuildId() > 0) {
                return getGuildById(guildPlayer.getGuildId()).join();
            }
            return null;
        });
    }

    /*
    * The guild will be obtained by its ID.
    * */
    public static CompletableFuture<Guild> getGuildById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String guildData = Redis.getRedis().get(String.valueOf(id)).join();
            ObjectMapper objectMapper = new ObjectMapper();

            Guild guild = null;

            try {
                guild = (guildData != null ? objectMapper.readValue(guildData, Guild.class) : MySQL.getMySQL().getGuildFromId(id).join());
                if(guild != null) guild.saveGuild();
            } catch (JsonProcessingException ignored) {}

            return guild;
        });
    }
}
