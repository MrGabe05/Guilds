package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.SimpleDateFormat;

/**
 * CommandInfo class represents the command to view guild information.
 */
public class CommandInfo extends GCommand {

    /**
     * Initializes a new CommandInfo instance.
     */
    public CommandInfo() {
        super("info", "guilds.command.info", true);
    }

    /**
     * Executes the 'info' command to view guild information.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
                // Check if the player is in a guild.
                if (guild == null) {
                    Lang.GUILD_NOT_HAVE.send(player);
                    return;
                }

                // Format date.
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                String dateFormat = format.format(guild.getDate());

                // Set placeholders and send guild information message.
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

        GuildPlayer.getPlayerByName(args[0]).thenAcceptAsync(guildTarget -> {
            if(guildTarget == null) {
                Lang.PLAYER_NOT_EXISTS.send(player);
                return;
            }

            Guild guild = Guild.getGuildById(guildTarget.getGuildId()).join();
            if (guild == null) {
                Lang.GUILD_NOT_EXISTS.send(player);
                return;
            }

            // Format date.
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
            String dateFormat = format.format(guild.getDate());

            // Set placeholders and send guild information message.
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
