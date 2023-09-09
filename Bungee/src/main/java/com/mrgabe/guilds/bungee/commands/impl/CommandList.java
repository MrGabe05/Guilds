package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * The CommandList class represents a command to list members of a guild.
 * It displays a list of guild members along with their joined date and rank.
 */
public class CommandList extends GCommand {

    /**
     * Creates a new instance of the CommandList class.
     */
    public CommandList() {
        super("list", "guild.command.list");
    }

    /**
     * Executes the list command.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");

            for (String m : Lang.GUILD_MEMBER_LIST.get(new Placeholders())) {
                if (m.startsWith("<members>")) {
                    for (UUID uuid : guild.fetchMembers().join()) {
                        GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(uuid).join();

                        Placeholders placeholders = new Placeholders();
                        placeholders.set("<members>", "");
                        placeholders.set("%player%", guildPlayer.getName());
                        placeholders.set("%joined_at%", format.format(guildPlayer.getJoined()));
                        placeholders.set("%rank%", guild.getRank(guildPlayer).getDisplayName());

                        player.sendMessage(Utils.color(placeholders.parse(m)));
                    }
                    continue;
                }

                player.sendMessage(Utils.color(m));
            }
        });
    }
}