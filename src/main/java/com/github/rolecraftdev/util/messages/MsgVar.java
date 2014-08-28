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
package com.github.rolecraftdev.util.messages;

/**
 * Represents a replaceable piece of text.
 *
 * @since 0.0.5
 */
public class MsgVar {
    /**
     * @since 0.0.5
     */
    public static final MsgVar SPELL = new MsgVar("$spell", "");
    /**
     * @since 0.0.5
     */
    public static final MsgVar PROFESSION = new MsgVar("$profession", "");
    /**
     * @since 0.0.5
     */
    public static final MsgVar GUILD = new MsgVar("$guild", "");
    /**
     * @since 0.0.5
     */
    public static final MsgVar PLAYER = new MsgVar("$player", "");
    /**
     * @since 0.0.5
     */
    public static final MsgVar RANK = new MsgVar("$rank", "");
    /**
     * @since 0.0.5
     */
    public static final MsgVar ACTION = new MsgVar("$action", "");
    /**
     * @since 0.0.5
     */
    public static final MsgVar VALUE = new MsgVar("$value", "");

    /**
     * The key of this {@link MsgVar}.
     */
    private final String var;
    /**
     * The value of this {@link MsgVar}.
     */
    private final String val;

    /**
     * Create a new {@link MsgVar}, in which the given value will be converted
     * to a string representation.
     *
     * @since 0.0.5
     * @see #MsgVar(String, String)
     */
    public MsgVar(final String var, final int val) {
        this(var, String.valueOf(val));
    }

    /**
     * Create a new {@link MsgVar}, in which the given value will be converted
     * to a string representation.
     *
     * @since 0.0.5
     * @see #MsgVar(String, String)
     */
    public MsgVar(final String var, final Object val) {
        this(var, String.valueOf(val));
    }

    /**
     * Constructor.
     *
     * @param var the key that should be replaced
     * @param val the value of the variable
     * @since 0.0.5
     */
    public MsgVar(final String var, final String val) {
        this.var = var;
        this.val = val;
    }

    /**
     * Replace the predefined piece of text with the set value.
     *
     * @param string the string in which the key should be replaced
     * @return the edited given string
     * @since 0.0.5
     */
    public String apply(final String string) {
        return string.replace(var, val);
    }

    /**
     * Create a new {@link MsgVar} with the same key, but with a different
     * value.
     *
     * @param val the new value
     * @return a new {@link MsgVar} with the old key and new value
     * @since 0.0.5
     */
    public MsgVar value(final String val) {
        return new MsgVar(var, val);
    }
}
