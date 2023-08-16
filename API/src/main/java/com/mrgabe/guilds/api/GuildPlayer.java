package com.mrgabe.guilds.api;

import lombok.Data;

import java.sql.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Data
public class GuildPlayer {

    private final UUID uuid;

    private int gangId, rank = -1;

    private Date joined = null;
    private UUID invited = null;
}
