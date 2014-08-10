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
package com.github.rolecraftdev.magic.spell;

import com.github.rolecraftdev.RolecraftCore;
import com.github.rolecraftdev.magic.spell.spells.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;

public class SpellManager {
    private final RolecraftCore plugin;
    private final Map<String, Spell> spells;
    private final int maxRange;
    private final Map<String, Boolean> emptyMap;

    public SpellManager(RolecraftCore parent) {
        this.plugin = parent;
        spells = new HashMap<String, Spell>();
        maxRange = parent.getConfig().getInt("magicrange", 100);
        emptyMap = new HashMap<String, Boolean>();

        // Tier 1 spells
        register("Freeze Block", new FreezeBlock(this));
        register("Burn Block", new BurnBlock(this));
        register("Lesser Sword", new LesserSword(this));
        register("Weak Bow", new WeakBow(this));

        // Tier 2 spells
        register("Freeze Ray", new FreezeRay(this));
        register("Fire Beam", new FireBeam(this));
        register("Farbreak", new Farbreak(this));
        register("Stronger Bow", new StrongerBow(this));
        register("Stronger Sword", new StrongerSword(this));
        register("Break Block", new BreakBlock(this));

        // Tier 3 spells
        register("Silk Touch", new SilkTouch(this));
        register("Excellent Bow", new ExcellentBow(this));
        register("Multi-Arrow", new MultiArrow(this));
        register("Arrow Shower", new ArrowShower(this));

        // Tier 4 spells
        register("Bomb", new Bomb(this));
        register("Meteor", new Meteor(this));
        register("Silky Farbreak", new FarbreakSilkTouch(this));
        register("Mining Hammer", new MiningHammer(this));
        register("Fly", new Fly(this));
        register("Hand Cannon", new HandCannon(this));

        // Tier 5 spells
        register("Avada Kedavra", new AvadaKedavra(this));
    }

    public void register(String wandName, Spell spell) {
        spells.put(wandName, spell);
        Bukkit.getPluginManager().addPermission(new Permission(
                "rolecraft.spell." + wandName.toLowerCase().replaceAll(" ", ""),
                "Allows access to the spell '" + wandName + "'",
                PermissionDefault.TRUE, emptyMap));
    }

    public Spell getSpell(String wandName) {
        return spells.get(wandName);
    }

    public float getMana(Player ply) {
        float mana = plugin.getDataManager().getPlayerData(ply.getUniqueId())
                .getMana();
        if (mana == -1) {
            return 0;
        } else {
            return mana;
        }
    }

    /**
     * Convience method
     *
     * @param ply
     * @param amount
     */
    public void subtractMana(Player ply, float amount) {
        plugin.getDataManager().getPlayerData(ply.getUniqueId())
                .substractMana(amount);
    }

    public void setMana(Player ply, float mana) {
        plugin.getDataManager().getPlayerData(ply.getUniqueId()).setMana(mana);
    }

    public int getMagicModfier(Player ply) {
        // TODO: make this work
        return 0;
    }

    public int getRange() {
        return maxRange;
    }

    public RolecraftCore getParent() {
        return plugin;
    }
}