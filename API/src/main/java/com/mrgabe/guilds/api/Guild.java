package com.mrgabe.guilds.api;

import lombok.Data;

import java.sql.Date;
import java.util.UUID;

@Data
public class Guild {

    private final int id;

    private final UUID owner;

    private final Settings settings;

    private String name, tag;

    private int kills, points, color;

    private Date date = new Date(System.currentTimeMillis());
}
