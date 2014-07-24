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
package com.github.rolecraftdev.guild;

import java.util.UUID;

/**
 * A representation of all of the actions which can be performed and are related
 * to guilds in one way or another
 */
public enum GuildAction {
    // TODO: More things might be added here
    KICK_MEMBER("kick"), INVITE("invite"), SET_HOME("sethome"), CHANGE_BLOCK(
            "modifyhall");

    /**
     * A player-readable version of the name of this action
     */
    private final String playerReadable;

    GuildAction(final String playerReadable) {
        this.playerReadable = playerReadable;
    }

    /**
     * Gets a player-readable version of the name of this guild action
     *
     * @return A player-readable version of this action's name
     */
    public String getPlayerReadableName() {
        return playerReadable;
    }

    /**
     * Checks whether the given player is permitted to perform this action
     * within the given guild - false will always be returned if the given
     * player is not a member of the given guild
     *
     * @param player The player to check the permissions of
     * @param guild  The guild to check the permissions of the given player in
     * @return true if the given player can perform this action, false otherwise
     */
    public boolean can(final UUID player, final Guild guild) {
        return guild.can(player, this);
    }
}
