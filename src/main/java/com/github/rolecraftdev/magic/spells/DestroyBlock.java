/*
 * This file is part of RolecraftCore.
 *
 * Copyright (c) 2014 RolecraftDev <http://rolecraftdev.github.com>
 * RolecraftCore is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this
 * license, visit http://creativecommons.org/licenses/by-nc-nd/3.0
 *
 * As long as you follow the following terms, you are free to copy and redistribute
 * the material in any medium or format.
 *
 * You must give appropriate credit, provide a link to the license, and indicate
 * whether any changes were made to the material. You may do so in any reasonable
 * manner, but not in any way which suggests the licensor endorses you or your use.
 *
 * You may not use the material for commercial purposes.
 *
 * If you remix, transform, or build upon the material, you may not distribute the
 * modified material.
 *
 * You may not apply legal terms or technological measures that legally restrict
 * others from doing anything the license permits.
 *
 * DISCLAIMER: This is a human-readable summary of (and not a substitute for) the
 * license.
 */
package com.github.rolecraftdev.magic.spells;

import com.github.rolecraftdev.magic.Spell;
import com.github.rolecraftdev.magic.SpellManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class DestroyBlock implements Spell {

    private SpellManager manager;

    public DestroyBlock(SpellManager parent) {
        manager = parent;
    }

    @Override
    public String getName() {
        return "Destroy Block";
    }

    @Override
    public float estimateAttackMana(Player ply, LivingEntity entity,
            int modifier) {
        return 0;
    }

    @Override
    public float estimateLeftClickMana(Player ply, Block block, int modifier, BlockFace face) {
        return 3;
    }

    @Override
    public float estimateRightClickMana(Player ply, Block block, int modifier, BlockFace face) {
        return 3;
    }

    @Override
    public float rightClick(Player ply, Block block, int modifier, BlockFace face) {
        BlockBreakEvent event = new BlockBreakEvent(block,ply);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            block.setType(Material.AIR);
        }
        return 3;
    }

    @Override
    public float leftClick(Player ply, Block block, int modifier, BlockFace face) {
        BlockBreakEvent event = new BlockBreakEvent(block,ply);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            block.setType(Material.AIR);
        }
        return 3;
    }

    @Override
    public float attack(Player ply, LivingEntity ent, int modifier) {
        return 0;
    }

    @Override
    public Recipe getWandRecipe() {
        ItemStack result = new ItemStack(Material.STICK);
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + getName());
        meta.addEnchant(Enchantment.LUCK, 10, true);
        result.setItemMeta(meta);
        ShapedRecipe recipe = new ShapedRecipe(result);
        // custom recipe stuff
        recipe.shape("AAB",
                     "ABA",
                     "BAA");
        recipe.setIngredient('A', Material.WOOD_PICKAXE);
        recipe.setIngredient('B', Material.IRON_INGOT);
        return recipe;
    }

}