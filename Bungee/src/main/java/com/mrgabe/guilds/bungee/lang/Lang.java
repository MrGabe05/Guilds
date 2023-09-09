package com.mrgabe.guilds.bungee.lang;

import com.mrgabe.guilds.bungee.Guilds;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.PluginLogger;
import com.mrgabe.guilds.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A language system that supports languages.
 */
public class Lang {

    private List<String> langs = new ArrayList<>();

    public static Lang
            UNKNOWN_ARGS,
            PLAYER_NEED,
            PLAYER_ONLY,
            PLAYER_MUTED,
            PLAYER_UNMUTED,
            PLAYER_HAS_GUILD,
            PLAYER_INVITATION,
            PLAYER_NOT_EXISTS,
            PLAYER_NOT_ONLINE,
            PLAYER_NOT_IN_GUILD,
            PLAYER_NOT_PERMISSIONS,
            PLAYER_TOGGLE_CHAT,
            GUILD_HELP,
            GUILD_CREATED,
            GUILD_DISBAND,
            GUILD_NOT_HAVE,
            GUILD_NOT_EXISTS,
            GUILD_NOT_INVITED,
            GUILD_ALREADY_HAVE,
            GUILD_ALREADY_INVITED,
            GUILD_PLAYER_JOINED,
            GUILD_PLAYER_KICKED,
            GUILD_PLAYER_INVITED,
            GUILD_PLAYER_LEAVE,
            GUILD_PLAYER_PROMOTE,
            GUILD_PLAYER_DEMOTE,
            GUILD_MEMBER_LIST,
            GUILD_MEMBER_ONLINE,
            GUILD_OWNER_TRANSFER,
            GUILD_NAME_UPDATED,
            GUILD_NOT_PERMISSIONS_FOR_TRANSFER,
            GUILD_NOT_PERMISSIONS_FOR_DISBAND,
            GUILD_NOT_PERMISSIONS_FOR_PROMOTE,
            GUILD_NOT_PERMISSIONS_FOR_DEMOTE,
            GUILD_NOT_PERMISSIONS_FOR_RENAME,
            GUILD_NOT_PERMISSIONS_FOR_INVITE,
            GUILD_NOT_PERMISSIONS_FOR_MUTE,
            GUILD_NOT_PERMISSIONS_FOR_KICK,
            GUILD_INFO;

    /**
     * Adds a list of language text to this Lang object.
     *
     * @param text The list of language text.
     */
    public void addLang(List<String> text) {
        langs = text.stream().map(Utils::color).collect(Collectors.toList());
    }

    public List<String> get(Placeholders placeholders) {
        return this.langs.stream().map(placeholders::parse).collect(Collectors.toList());
    }

    /**
     * Sends the language messages to a CommandSender without any placeholders.
     *
     * @param sender The CommandSender to send the messages to.
     */
    public void send(CommandSender sender) {
        send(sender, new Placeholders());
    }

    /**
     * Sends the language messages to a CommandSender with placeholders.
     *
     * @param sender      The CommandSender to send the messages to.
     * @param placeholders The placeholders to apply to the messages.
     */
    public void send(CommandSender sender, Placeholders placeholders) {
        for (String s : this.langs) {
            s = placeholders.parse(s);

            sender.sendMessage(Utils.color(s));
        }
    }

    /**
     * Loads predefined language objects from a configuration file.
     */
    public static void loadLangs() {
        File configFile = new File(Guilds.getInstance().getDataFolder(), "Lang.yml");
        if (!configFile.exists()) {
            try {
                try (InputStream is = Guilds.getInstance().getResourceAsStream("Lang.yml")) {
                    Files.copy(is, configFile.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error creating the config file.", e);
            }
        }

        Configuration langConfig;
        try {
            langConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Error loading the config file.", e);
        }

        for (Field field : Lang.class.getFields()) {
            field.setAccessible(true);

            if (field.getType() == Lang.class) {
                try {
                    if (field.get(null) == null) {
                        field.set(field, new Lang());
                    }

                    Lang obj = (Lang) field.get(null);
                    obj.addLang(langConfig.getStringList(field.getName().toLowerCase(Locale.ROOT)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        PluginLogger.info("Lang loaded.");
    }
}

