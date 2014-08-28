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
package com.github.rolecraftdev;

import com.github.rolecraftdev.command.guild.GuildCommand;
import com.github.rolecraftdev.command.other.DebugCommand;
import com.github.rolecraftdev.command.other.GCCommand;
import com.github.rolecraftdev.command.other.RCConfirmCommand;
import com.github.rolecraftdev.command.profession.ProfessionCommand;
import com.github.rolecraftdev.data.DataManager;
import com.github.rolecraftdev.data.storage.DataStore;
import com.github.rolecraftdev.data.storage.MySQLDataStore;
import com.github.rolecraftdev.data.storage.SQLiteDataStore;
import com.github.rolecraftdev.data.storage.YamlFile;
import com.github.rolecraftdev.event.RolecraftEventFactory;
import com.github.rolecraftdev.guild.Guild;
import com.github.rolecraftdev.guild.GuildManager;
import com.github.rolecraftdev.magic.Spell;
import com.github.rolecraftdev.magic.SpellManager;
import com.github.rolecraftdev.profession.Profession;
import com.github.rolecraftdev.profession.ProfessionManager;
import com.github.rolecraftdev.quest.Quest;
import com.github.rolecraftdev.quest.QuestManager;
import com.github.rolecraftdev.util.messages.Messages;
import com.github.rolecraftdev.util.messages.MsgVar;

import net.milkbowl.vault.economy.Economy;

import pw.ian.albkit.AlbPlugin;

import javax.annotation.Nullable;
import java.util.logging.Logger;

/**
 * Main class for the core of Rolecraft, the Bukkit RPG plugin.
 *
 * @since 0.0.5
 */
public final class RolecraftCore extends AlbPlugin {
    /**
     * Whether the storage solution has finished initialisation and is thus
     * fully loaded.
     */
    private volatile boolean sqlLoaded;

    /**
     * The Rolecraft {@link Logger}.
     */
    private Logger logger;
    /**
     * All the messages used by Rolecraft.
     */
    private Messages messages;
    /**
     * Manages Rolecraft's persistent data.
     */
    private DataManager dataManager;
    /**
     * Manages Rolecraft's {@link Guild}s.
     */
    private GuildManager guildManager;
    /**
     * Manages Rolecraft's {@link Quest}s.
     */
    private QuestManager questManager;
    /**
     * Manages Rolecraft's {@link Profession}s.
     */
    private ProfessionManager professionManager;
    /**
     * Manages Rolecraft's {@link Spell}s
     */
    private SpellManager spellManager;
    /**
     * The Vault {@link Economy} instance.
     */
    private Economy economy;

    // Configuration options

    /**
     * The amount of negative karma a player starts with.
     */
    private float originalSin;
    /**
     * Whether to call extra events.
     */
    private boolean extraEvents;

    /**
     * @since 0.0.5
     */
    @Override
    public void onEnable() {
        super.init();

        logger = getLogger();

        // Check for Vault to decide whether to enable economy support
        economy = hookVaultEcon();
        if (!vaultEconHooked()) {
            // Warn the admin that no economy was found
            logger.warning("Couldn't find Vault, disabling economy support");
        }

        // Create default configuration file if it doesn't exist already
        final YamlFile config = new YamlFile(this, "config.yml", false);
        // Get options from the config
        final String dbType = config.getString("sqlserver").toLowerCase();
        extraEvents = config.getBoolean("extraevents");
        originalSin = (float) config.getDouble("originalsin");

        // Set the plugin object for event construction in RolecraftEventFactory
        RolecraftEventFactory.setPlugin(this);

        // Load all plugin messages from the messages file
        messages = new Messages(this);
        messages.load();

        // Set up the Rolecraft database solution
        final DataStore dataStore;

        if (dbType.equals("sqlite")) {
            dataStore = new SQLiteDataStore(this);
        } else if (dbType.equals("mysql")) {
            dataStore = new MySQLDataStore(this);
        } else {
            logger.warning(
                    "SQLServer in config was not one of: \"sqlite\" or \"mysql\", defaulting to sqlite");
            dataStore = new SQLiteDataStore(this);
        }

        // Log the data store we are using
        logger.info("Using " + dataStore.getStoreTypeName()
                + " for Rolecraft data!");

        // Initialise DataStore
        dataStore.initialise();
        // Cleanup quests in database
        // dataStore.finalizeQuests(questManager);

        // Create all the manager objects / load data
        dataManager = new DataManager(this, dataStore);
        guildManager = new GuildManager(this);
        questManager = new QuestManager(this);
        professionManager = new ProfessionManager(this);
        spellManager = new SpellManager(this);

        professionManager.loadProfessions();

        // Register listener
        register(new RCListener(this));

        // Register commands
        register(new GuildCommand(this));
        register(new ProfessionCommand(this));
        register(new GCCommand(this));
        register(new RCConfirmCommand(this));
        register(new DebugCommand(this));
    }

    /**
     * @since 0.0.5
     */
    @Override
    public void onDisable() {
        dataManager.unloadAllPlayerData();
    }

    /**
     * Get the configured messages used by Rolecraft.
     *
     * @return the configured {@link Messages}
     * @since 0.0.5
     */
    public Messages getMessages() {
        return messages;
    }

    /**
     * Retrieves the value of configured message associated to the given key and
     * applies the specified variables to it.
     *
     * @param key the key of which the value (message) should be returned
     * @param vars the {@link MsgVar}s that should be applied to the message for
     *        the specified key
     * @return the configured message for the given key
     * @since 0.0.5
     */
    @Nullable
    public String getMessage(final String key, final MsgVar... vars) {
        return getMessages().get(key, vars);
    }

    /**
     * Get the used {@link DataStore} implementation, which is configured by the
     * user.
     *
     * @return the used {@link DataStore} implementation
     * @since 0.0.5
     */
    public DataStore getDataStore() {
        return dataManager.getStore();
    }

    /**
     * Gets the used {@link DataManager}.
     *
     * @return the used {@link DataManager}
     * @since 0.0.5
     */
    public DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Get the {@link GuildManager} that is responsible for managing all
     * available {@link Guild}s.
     *
     * @return the used {@link GuildManager}
     * @since 0.0.5
     */
    public GuildManager getGuildManager() {
        return guildManager;
    }

    /**
     * Get the {@link ProfessionManager} that is responsible for managing all
     * available {@link Profession}s.
     *
     * @return the used {@link ProfessionManager}
     * @since 0.0.5
     */
    public ProfessionManager getProfessionManager() {
        return professionManager;
    }

    /**
     * Get the {@link QuestManager} that is responsible for managing all
     * available {@link Quest}s.
     *
     * @return the used {@link QuestManager}
     * @since 0.0.5
     */
    public QuestManager getQuestManager() {
        return questManager;
    }

    /**
     * Get the {@link SpellManager} that is responsible for managing all
     * available {@link Spell}s.
     *
     * @return the used {@link SpellManager}
     * @since 0.0.5
     */
    public SpellManager getSpellManager() {
        return spellManager;
    }

    /**
     * Gets Vault's {@link Economy} instance that is currently in use.
     *
     * @return Vault's {@link Economy}
     * @since 0.0.5
     */
    @Nullable
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Check whether economy is available and in use.
     *
     * @return {@code true} if economy is available and in use; {@code false}
     *         otherwise
     * @since 0.0.5
     */
    public boolean doesUseEconomy() {
        return vaultEconHooked();
    }

    /**
     * Check if the use of extra events is enabled.
     *
     * @return {@code true} if extra events are enabled
     * @since 0.0.5
     */
    public boolean isExtraEvents() {
        return extraEvents;
    }

    /**
     * Get the amount of negative karma a player should start with when he joins
     * for the first time or respawns.
     *
     * @return the negative karma level a player begins with
     * @since 0.0.5
     */
    public float getOriginalSin() {
        return originalSin;
    }

    /**
     * Returns whether the used storage implementation has finished
     * initialisation and is thus completely loaded.
     *
     * @return only {@code true} if the storage system has fully been made
     *         available on this Minecraft server
     * @since 0.0.5
     */
    public boolean isSqlLoaded() {
        return sqlLoaded;
    }

    /**
     * Mark the state of the used storage mechanism as wholly loaded on this
     * Minecraft server. Calling this method from any code outside the used
     * {@link DataStore} implementation can result in unknown problems.
     *
     * @param loaded the new state of the used storage mechanism
     * @since 0.0.5
     */
    public void setSqlLoaded(final boolean loaded) {
        sqlLoaded = loaded;
    }
}
