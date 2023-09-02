package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;

public class CommandInfo extends GCommand {

    public CommandInfo() {
        super("info", "guilds.command.info", true);
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
                if (guild == null) {
                    Lang.GUILD_NOT_HAVE.send(player);
                    return;
                }

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                String dateFormat = format.format(guild.getDate());

                Placeholders placeholders = new Placeholders();
                placeholders.set("%id%", guild.getId());
                placeholders.set("%tag%", guild.getTag());
                placeholders.set("%owner%", guild.getOwner().getName());
                placeholders.set("%date%", dateFormat);
                placeholders.set("%kills%", guild.getKills());
                placeholders.set("%points%", guild.getPoints());

                Lang.GUILD_INFO.send(player, placeholders);
            });
            return;
        }

        Object target;

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
        if(targetPlayer.hasPlayedBefore()) {
            target = targetPlayer.getUniqueId();
        } else {
            target = args[0];
        }

        Guild.getGuildByMember(target).thenAcceptAsync(guild -> {
            if (guild == null) {
                Lang.GUILD_NOT_EXISTS.send(player);
                return;
            }

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
            String dateFormat = format.format(guild.getDate());

            Placeholders placeholders = new Placeholders();
            placeholders.set("%id%", guild.getId());
            placeholders.set("%tag%", guild.getTag());
            placeholders.set("%owner%", guild.getOwner().getName());
            placeholders.set("%date%", dateFormat);
            placeholders.set("%kills%", guild.getKills());
            placeholders.set("%points%", guild.getPoints());

            Lang.GUILD_INFO.send(player, placeholders);
        });
    }
}
