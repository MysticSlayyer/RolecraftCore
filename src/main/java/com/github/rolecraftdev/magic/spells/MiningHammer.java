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
import com.github.rolecraftdev.util.SoundWrapper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Spell} implementation which will make the held item function like a
 * pickaxe, although this will affect more blocks when used.
 *
 * @since 0.0.5
 */
public class MiningHammer implements Spell {
    private final SpellManager spellManager;

    /**
     * Constructor.
     *
     * @param spellManager the {@link SpellManager} this {@link Spell}
     *        implementation will be registered to
     * @since 0.0.5
     */
    public MiningHammer(final SpellManager spellManager) {
        this.spellManager = spellManager;
    }

    /**
     * Allows opposing directions to be easily handled as equal.
     *
     * @since 0.0.5
     */
    private enum Orientation {
        /**
         * Z axis - north or south.
         *
         * @since 0.0.5
         */
        NORTHSOUTH,
        /**
         * X axis - east or west.
         *
         * @since 0.0.5
         */
        EASTWEST,
        /**
         * Y axis - up or down.
         *
         * @since 0.0.5
         */
        FLAT
    }

    /**
     * @since 0.0.5
     */
    @Override
    public String getName() {
        return this.spellManager.getConfiguredName(this.getDefaultName());
    }

    /**
     * @since 0.1.0
     */
    @Override
    public String getDefaultName() {
        return "Mining Hammer";
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float estimateAttackMana(final Player caster,
            final LivingEntity target, final int modifier) {
        return 0;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float estimateLeftClickMana(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        return estimateClickMana(caster, block, modifier, face);
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float estimateRightClickMana(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        return estimateClickMana(caster, block, modifier, face);
    }

    private float estimateClickMana(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        return (30f - modifier / 100f > 0) ? 30f - modifier / 100f : 0;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float rightClick(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        return click(caster, block, modifier, face);
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float leftClick(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        return click(caster, block, modifier, face);
    }

    private float click(final Player ply, final Block block,
            final int modifier, final BlockFace face) {
        if (block == null) {
            return CAST_FAILURE;
        }

        List<Block> blocks;
        if (face == BlockFace.DOWN || face == BlockFace.UP) {
            blocks = getBlocksAround(block, Orientation.FLAT);
        } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
            blocks = getBlocksAround(block, Orientation.EASTWEST);
        } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            blocks = getBlocksAround(block, Orientation.NORTHSOUTH);
        } else {
            return CAST_FAILURE;
        }

        for (final Block toBreak : blocks) {
            if (spellManager.getPlugin().getConfigValues().isExtraEvents()) {
                final BlockBreakEvent event = new BlockBreakEvent(toBreak, ply);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    continue;
                }
            }
            toBreak.breakNaturally();
        }
        return estimateClickMana(ply, block, modifier, face);
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float attack(final Player caster, final LivingEntity target,
            final int modifier) {
        return BAD_SITUATION;
    }

    /**
     * Acquire all {@link Block}s around (as a plane) and including, the given
     * center {@link Block} depending on the given {@link Orientation}, which
     * works as normal of the used plane.
     *
     * @param center the center {@link Block}
     * @param orientation the used {@link Orientation}, which functions as
     *        normal
     * @return the {@link Block}s around the given center {@link Block} in a
     *         plane
     */
    private List<Block> getBlocksAround(final Block center,
            final Orientation orientation) {
        if (center == null) {
            return null;
        }

        final List<Block> temp = new ArrayList<Block>(9);
        if (orientation == Orientation.NORTHSOUTH) {
            final int z = center.getZ();
            for (int y = center.getY() - 1; y <= center.getY() + 1; y++) {
                for (int x = center.getX() - 1; x <= center.getX() + 1; x++) {
                    temp.add(center.getWorld().getBlockAt(x, y, z));
                }
            }
        } else if (orientation == Orientation.EASTWEST) {
            final int x = center.getX();
            for (int y = center.getY() - 1; y <= center.getY() + 1; y++) {
                for (int z = center.getZ() - 1; z <= center.getZ() + 1; z++) {
                    temp.add(center.getWorld().getBlockAt(x, y, z));
                }
            }
        } else if (orientation == Orientation.FLAT) {
            final int y = center.getY();
            for (int z = center.getZ() - 1; z <= center.getZ() + 1; z++) {
                for (int x = center.getX() - 1; x <= center.getX() + 1; x++) {
                    temp.add(center.getWorld().getBlockAt(x, y, z));
                }
            }
        } else {
            return null;
        }

        return temp;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public ShapedRecipe getWandRecipe() {
        final ItemStack result = new ItemStack(Material.STICK);
        final ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + getName());
        meta.addEnchant(Enchantment.LUCK, 10, true);
        result.setItemMeta(meta);
        final ShapedRecipe recipe = new ShapedRecipe(result);
        // custom recipe stuff
        recipe.shape("APB",
                "PBP",
                "BPA");
        recipe.setIngredient('A', Material.BOW);
        recipe.setIngredient('P', Material.DIAMOND_PICKAXE);
        recipe.setIngredient('B', Material.IRON_BLOCK);
        return recipe;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public SoundWrapper getSound() {
        return new SoundWrapper(Sound.FIREWORK_LARGE_BLAST, 1.0f, 0f);
    }
}
