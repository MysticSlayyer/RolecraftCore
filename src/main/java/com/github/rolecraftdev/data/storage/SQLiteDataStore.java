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
package com.github.rolecraftdev.data.storage;

import com.github.rolecraftdev.RolecraftCore;
import com.github.rolecraftdev.data.PlayerData;
import com.github.rolecraftdev.guild.Guild;

import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public final class SQLiteDataStore extends DataStore {

    private Connection connection;

    public SQLiteDataStore(RolecraftCore parent) {
        super(parent);
    }
    
    public static final String dbname = "rolecraft";

    private static final String createPlayerTable = "CREATE TABLE IF NOT EXISTS " + pt + " ("
            + "uuid VARCHAR(37) PRIMARY KEY ON CONFLICT REPLACE,"
            + "lastname VARCHAR(16) NOT NULL ON CONFLICT FAIL,"
            + "guild REFERENCES "+ gt + "(uuid) ON DELETE SET NULL,"
            + "exp REAL DEFAULT 0,"
            + "profession VARCHAR (37) DEFAULT NULL,"
            + "influence INTEGER DEFAULT 0" + ")";

    private static final String createGuildTable = "CREATE TABLE IF NOT EXISTS "+ gt + " ("
            + "uuid VARCHAR(37) PRIMARY KEY ON CONFLICT FAIL,"
            + "name VARCHAR (50),"
            + "leader VARCHAR(37),"
            + "members TEXT,"
            + "ranks TEXT,"
            + "home VARCHAR(150),"
            + "hall VARCHAR(100),"
            + "influence INTEGER DEFAULT 0" + ")";

    @Override
    public void intialise() {
        final RolecraftCore parent = this.getParent();
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = connection.prepareStatement(createPlayerTable);
                    ps.execute();
                    ps.close();
                    ps = connection.prepareStatement(createGuildTable);
                    ps.execute();
                    ps.close();
                    parent.setSqlLoaded(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(getParent());

    }

    @Override
    protected Connection getConnection() {
        File dataFile = new File(getParent().getDataFolder(), dbname+".db");
        if (!dataFile.exists()){
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getParent().getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");            
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
            return connection;
        } catch (SQLException ex) {
            getParent().getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            getParent().getLogger().log(Level.SEVERE, "CraftBukkit build error");
        }
        return null;
    }

    @Override
    public String getStoreTypeName() {
        return "SQLite";

    }

    /**
     * Do not pull up
     * 
     * @see com.github.rolecraftdev.data.storage.DataStore#clearPlayerData(com.github.rolecraftdev.data.PlayerData)
     */
    @Override
    public void clearPlayerData(final PlayerData data) {
        data.setUnloading(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection connection = getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = connection.prepareStatement("INSERT INTO " + pt
                            + " (uuid, name) VALUES (?,?)");
                    ps.setString(1, data.getPlayerId().toString());
                    ps.setString(2, data.getPlayerName());
                    ps.execute();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(getParent());
    }

}
