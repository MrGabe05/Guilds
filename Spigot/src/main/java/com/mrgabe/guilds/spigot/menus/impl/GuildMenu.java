package com.mrgabe.guilds.spigot.menus.impl;

import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.menus.Menu;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class GuildMenu extends Menu {

    public GuildMenu() {
        super(new YamlConfig(Guilds.getInstance(), "menus/Guild"), new Placeholders());
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onClick(InventoryClickEvent event) {

    }
}
