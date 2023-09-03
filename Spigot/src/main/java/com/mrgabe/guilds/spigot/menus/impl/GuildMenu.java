package com.mrgabe.guilds.spigot.menus.impl;

import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.menus.Menu;
import com.mrgabe.guilds.spigot.utils.ItemBuilder;
import com.mrgabe.guilds.utils.Placeholders;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Map;

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

    public void load(Player player) {
        for(Map.Entry<Integer, ItemBuilder> itemStackEntry : this.getFillItems().entrySet()) {
            ItemBuilder itemBuilder = itemStackEntry.getValue().clone();
            if(itemStackEntry.getKey() >= 0) {
                this.setItem(itemStackEntry.getKey(), itemBuilder.build(player, new Placeholders()));
            }
        }
    }
}
