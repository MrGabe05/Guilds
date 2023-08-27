package com.mrgabe.guilds.spigot.utils;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public final class ItemBuilder implements Cloneable {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private boolean textured = false;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material type){
        itemStack = new ItemStack(type, 1);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder asSkullOf(OfflinePlayer player) {
        itemStack.setType(XMaterial.PLAYER_HEAD.parseMaterial());

        if(itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;

            skullMeta.setOwner(player.getName());
            textured = true;
        }
        return this;
    }

    public ItemBuilder withName(String name){
        if(name != null)
            itemMeta.setDisplayName(Utils.color(name));
        return this;
    }

    public ItemBuilder withLore(List<String> lore, Placeholders placeholders){
        if(lore != null)
            itemMeta.setLore(lore.stream().map(Utils::color).map(placeholders::parse).collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchant, int level){
        itemMeta.addEnchant(enchant, level, true);
        return this;
    }

    public ItemBuilder withFlags(ItemFlag... itemFlags){
        itemMeta.addItemFlags(itemFlags);
        return this;
    }

    public void setGlow() {
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder setUnbreakable() {
        Method method;

        try {
            if(Version.getVersion() > 13) {
                method = Class.forName("org.bukkit.inventory.meta.ItemMeta").getMethod("setUnbreakable", boolean.class);
            } else {
                method = Class.forName("org.bukkit.inventory.meta.ItemMeta.Spigot").getMethod("setUnbreakable", boolean.class);
            }

            method.invoke(itemMeta, true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ItemBuilder setColor(String sColor) {
        Color color;
        try {
            color = (Color) Color.class.getDeclaredField(sColor.toUpperCase()).get(Color.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return this;
        }

        if(itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
            leatherMeta.setColor(color);
        }
        return this;
    }

    public ItemStack build(OfflinePlayer offlinePlayer, Placeholders placeholders) {
        placeholders.set("%player%", (offlinePlayer.isOnline() ? "&a" + offlinePlayer.getName() : "&c" + offlinePlayer.getName()));

        if(itemStack.getType() == XMaterial.PLAYER_HEAD.parseMaterial() && !textured)
            asSkullOf(offlinePlayer);

        if(itemMeta.hasDisplayName())
            withName(placeholders.parse(itemMeta.getDisplayName()));

        if(itemMeta.hasLore())
            withLore(itemMeta.getLore(), placeholders);

        return build();
    }

    public ItemStack build(Placeholders placeholders) {
        if(itemMeta.hasDisplayName())
            withName(placeholders.parse(itemMeta.getDisplayName()));

        if(itemMeta.hasLore())
            withLore(itemMeta.getLore(), placeholders);

        return build();
    }

    public ItemStack build(){
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemBuilder clone(){
        try {
            ItemBuilder itemBuilder = (ItemBuilder) super.clone();
            itemBuilder.itemStack = itemStack.clone();
            itemBuilder.itemMeta = itemMeta.clone();
            itemBuilder.textured = textured;
            return itemBuilder;
        }catch(Exception ex){
            throw new NullPointerException(ex.getMessage());
        }
    }
}
