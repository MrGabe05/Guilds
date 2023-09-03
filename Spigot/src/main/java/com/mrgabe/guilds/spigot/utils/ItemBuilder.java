package com.mrgabe.guilds.spigot.utils;

import com.cryptomorin.xseries.XMaterial;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.Utils;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for building and customizing ItemStacks in Bukkit/Spigot.
 */
@Getter
public final class ItemBuilder implements Cloneable {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private boolean textured = false;

    /**
     * Constructs an ItemBuilder with an existing ItemStack.
     *
     * @param itemStack The base ItemStack to start with.
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Constructs an ItemBuilder with a specified Material.
     *
     * @param type The Material to create an ItemStack with.
     */
    public ItemBuilder(Material type){
        itemStack = new ItemStack(type, 1);
        itemMeta = itemStack.getItemMeta();
    }

    /**
     * Sets the ItemStack to be a player skull with the owner's name.
     *
     * @param player The OfflinePlayer representing the skull's owner.
     * @return The updated ItemBuilder.
     */
    public ItemBuilder asSkullOf(OfflinePlayer player) {
        itemStack.setType(XMaterial.PLAYER_HEAD.parseMaterial());

        if(itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;

            skullMeta.setOwner(player.getName());
            textured = true;
        }
        return this;
    }

    /**
     * Sets the display name of the ItemStack.
     *
     * @param name The name to set.
     * @return The updated ItemBuilder.
     */
    public ItemBuilder withName(String name){
        if(name != null)
            itemMeta.setDisplayName(Utils.color(name));
        return this;
    }

    /**
     * Sets the lore of the ItemStack and applies placeholders.
     *
     * @param lore        The lore to set.
     * @param placeholders The placeholders to apply to the lore.
     * @return The updated ItemBuilder.
     */
    public ItemBuilder withLore(List<String> lore, Placeholders placeholders){
        if(lore != null)
            itemMeta.setLore(lore.stream().map(Utils::color).map(placeholders::parse).collect(Collectors.toList()));
        return this;
    }

    /**
     * Adds an enchantment to the ItemStack.
     *
     * @param enchant The enchantment to add.
     * @param level   The level of the enchantment.
     * @return The updated ItemBuilder.
     */
    public ItemBuilder withEnchant(Enchantment enchant, int level){
        itemMeta.addEnchant(enchant, level, true);
        return this;
    }

    /**
     * Sets item flags on the ItemStack.
     *
     * @param itemFlags The ItemFlags to set.
     * @return The updated ItemBuilder.
     */
    public ItemBuilder withFlags(ItemFlag... itemFlags){
        itemMeta.addItemFlags(itemFlags);
        return this;
    }

    /**
     * Sets the glow effect on the ItemStack.
     */
    public void setGlow() {
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    /**
     * Sets the color of leather armor ItemStack.
     *
     * @param sColor The color to set (e.g., "RED").
     * @return The updated ItemBuilder.
     */
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

    /**
     * Builds the ItemStack with placeholders for the display name and lore.
     *
     * @param offlinePlayer The OfflinePlayer for placeholder substitution.
     * @param placeholders  The placeholders to apply to the display name and lore.
     * @return The constructed ItemStack.
     */
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

    /**
     * Builds the ItemStack with placeholders for the display name and lore.
     *
     * @param placeholders The placeholders to apply to the display name and lore.
     * @return The constructed ItemStack.
     */
    public ItemStack build(Placeholders placeholders) {
        if(itemMeta.hasDisplayName())
            withName(placeholders.parse(itemMeta.getDisplayName()));

        if(itemMeta.hasLore())
            withLore(itemMeta.getLore(), placeholders);

        return build();
    }

    /**
     * Builds and returns the final ItemStack.
     *
     * @return The constructed ItemStack.
     */
    public ItemStack build(){
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /**
     * Creates a deep clone of the ItemBuilder.
     *
     * @return A cloned ItemBuilder.
     */
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
