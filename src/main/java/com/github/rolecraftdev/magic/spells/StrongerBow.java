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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A {@link Spell} implementation that will shoot an arrow.
 *
 * @since 0.0.5
 */
public class StrongerBow implements Spell {
    private final SpellManager spellManager;

    /**
     * Constructor.
     *
     * @param spellManager the {@link SpellManager} this {@link Spell}
     *        implementation will be registered to
     * @since 0.0.5
     */
    public StrongerBow(final SpellManager spellManager) {
        this.spellManager = spellManager;
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
        return "Strong Bow";
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
        return 0;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float estimateRightClickMana(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        return (50f - modifier / 100f > 0) ? 50f - modifier / 100f : 0;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float rightClick(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        if (block != null) {
            return CAST_FAILURE;
        }

        caster.launchProjectile(Arrow.class);
        //arrow.setVelocity(arrow.getVelocity().multiply(2d));
        //arrow.setMetadata("Multiplier", new FixedMetadataValue(
        //        manager.getPlugin(), new Float(2f)));

        return estimateRightClickMana(caster, block, modifier, face);
    }

    /**
     * @since 0.0.5
     */
    @Override
    public float leftClick(final Player caster, final Block block,
            final int modifier, final BlockFace face) {
        return BAD_SITUATION;
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
        recipe.setIngredient('A', Material.ARROW);
        recipe.setIngredient('P', Material.BOW);
        recipe.setIngredient('B', Material.IRON_BLOCK);
        return recipe;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public SoundWrapper getSound() {
        return SoundWrapper.BOW;
    }
}
