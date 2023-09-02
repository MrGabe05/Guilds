package com.mrgabe.guilds.spigot.menus.impl;

import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.menus.Menu;
import com.mrgabe.guilds.spigot.utils.ItemBuilder;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import com.mrgabe.guilds.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Map;
import java.util.function.Consumer;

public class ConfirmMenu extends Menu {

    private final Consumer<Boolean> action;

    public ConfirmMenu(Consumer<Boolean> action) {
        super(new YamlConfig(Guilds.getInstance(), "menus/Confirm"), new Placeholders());

        this.action = action;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.load((Player) event.getPlayer());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            if(Utils.parseBoolean(this.getActions().get(event.getSlot()))) {
                this.action.accept(true);
            }

            event.getWhoClicked().closeInventory();
        }
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