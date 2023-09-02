package com.mrgabe.guilds.spigot.lang;

import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import com.mrgabe.guilds.utils.PluginLogger;
import com.mrgabe.guilds.utils.Utils;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
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
            GUILD_HELP,
            GUILD_NOT_HAVE,
            GUILD_NOT_EXISTS,
            GUILD_INFO;

    /**
     * Adds a list of language text to this Lang object.
     *
     * @param text The list of language text.
     */
    public void addLang(List<String> text) {
        langs = text.stream().map(Utils::color).collect(Collectors.toList());
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
        YamlConfig langConfig = new YamlConfig(Guilds.getInstance(), "Lang");

        for (Field field : Lang.class.getFields()) {
            field.setAccessible(true);

            if (field.getType() == Lang.class) {
                if (!langConfig.isSet(field.getName().toLowerCase(Locale.ROOT))) {
                    langConfig.set(field.getName().toLowerCase(Locale.ROOT), field.getName().toLowerCase(Locale.ROOT) + " value not set");
                }

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
        langConfig.save();

        PluginLogger.info("Lang loaded.");
    }
}

