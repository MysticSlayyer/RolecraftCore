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

import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MySQL {@link DataStore} implementation.
 *
 * @since 0.0.5
 */
@SuppressWarnings("FeatureEnvy")
public final class MySQLDataStore extends DataStore {
    /**
     * The query used for creating the player table in the database.
     */
    private static final String CREATE_PLAYER_TABLE =
            "CREATE TABLE IF NOT EXISTS "
                    + pt
                    + " ("
                    + "uuid VARCHAR(40) PRIMARY KEY,"
                    + "lastname VARCHAR(16) NOT NULL,"
                    + "FOREIGN KEY (guild) REFERENCES "
                    + gt
                    + "(uuid) ON DELETE SET NULL,"
                    + "exp REAL DEFAULT 0,"
                    + "profession VARCHAR (37) DEFAULT NULL"
                    + "secondprofession VARCHAR(37) DEFAULT NULL,"
                    + "influence INTEGER DEFAULT 0,"
                    + "karma REAL DEFAULT 0,"
                    + "mana REAL DEFAULT 0," + "settings VARCHAR(100)" + ")";
    /**
     * The query used for creating the guild table in the database.
     */
    private static final String CREATE_GUILD_TABLE =
            "CREATE TABLE IF NOT EXISTS "
                    + gt
                    + " ("
                    + "uuid VARCHAR(37) PRIMARY KEY ON CONFLICT FAIL,"
                    + "name VARCHAR (50),"
                    + "leader VARCHAR(37),"
                    + "members MEDIUMTEXT,"
                    + "ranks MEDIUMTEXT,"
                    + "home VARCHAR(150),"
                    + "influence INTEGER DEFAULT 0," +
                    "open BOOLEAN DEFAULT FALSE" + ")";
    /**
     * The query used for creating the metadata table in the database.
     */
    private static final String CREATE_META_TABLE =
            "CREATE TABLE IF NOT EXISTS "
                    + mdt
                    + " ("
                    + "version VARCHAR(6),"
                    + "entry VARCHAR(20),"
                    + "PRIMARY KEY(entry)" + ")";
    //                                MINUTES
    private static final int MINUTES = 60000;
    private static final int KILL_TIME = 5 * MINUTES;
    private static final int MYSQL_DEFAULT_PORT = 3306;

    /**
     * The username used to access the database.
     */
    private final String user;
    /**
     * The password used to access the database.
     */
    private final String password;
    /**
     * The port used for connecting to the database.
     */
    private final int port;
    /**
     * The URI used for connecting to the database.
     */
    private final String uri;
    /**
     * The name of the Rolecraft database.
     */
    private final String databaseName;

    //                              connection      in use? last use
    private final ConcurrentHashMap<Connection, Entry<Boolean, Long>> connections;

    /**
     * Constructor.
     *
     * @param plugin the associated {@link RolecraftCore} instance
     * @since 0.0.5
     */
    public MySQLDataStore(final RolecraftCore plugin) {
        super(plugin);

        user = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");
        uri = plugin.getConfig().getString("mysql.address");
        port = plugin.getConfig().getInt("mysql.port", MYSQL_DEFAULT_PORT);
        databaseName = plugin.getConfig().getString("mysql.databasename");

        connections = new ConcurrentHashMap<Connection, Entry<Boolean, Long>>();

        new BukkitRunnable() {
            @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
            @Override
            public void run() {
                final Iterator<Entry<Connection, Entry<Boolean, Long>>> iter = connections
                        .entrySet().iterator();
                while (iter.hasNext()) {
                    final Entry<Connection, Entry<Boolean, Long>> conn = iter
                            .next();
                    try {
                        if (conn.getKey() == null || conn.getKey().isClosed()) {
                            // if in use, reset timer
                            if (conn.getValue().getKey()) {
                                conn.getValue()
                                        .setValue(System.currentTimeMillis());
                            }
                            // else check for age and close and remove/keepalive
                            else {
                                if (conn.getValue().getValue() + KILL_TIME
                                        < System.currentTimeMillis()) {
                                    if (!connections.isEmpty()) {
                                        conn.getKey().close();
                                        iter.remove();
                                    } else {
                                        // keepalive
                                        conn.getKey().prepareCall("SELECT 1")
                                                .execute();
                                    }
                                } else {
                                    // keepalive
                                    conn.getKey().prepareCall("SELECT 1")
                                            .execute();
                                }
                            }
                        }
                    } catch (final SQLException e) {

                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimerAsynchronously(getPlugin(), 20 * 20, 20 * 20);
    }

    /**
     * @since 0.0.5
     */
    @Override
    public void initialise() {
        final RolecraftCore parent = getPlugin();
        new BukkitRunnable() {
            @Override
            public void run() {
                final Connection connection = getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = connection.prepareStatement(CREATE_PLAYER_TABLE);
                    ps.execute();
                    ps.close();
                    ps = connection.prepareStatement(CREATE_GUILD_TABLE);
                    ps.execute();
                    ps.close();
                    ps = connection.prepareStatement(CREATE_META_TABLE);
                    ps.execute();
                    ps.close();

                    ps = connection.prepareStatement(
                            "SELECT version FROM " + mdt + " WHERE entry = ?");
                    ps.setString(1, mde);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (!rs.getString("version")
                                .equals(DataStore.SQLVERSION1)) {
                            // TODO: in the future versions, add logic to update database
                        }
                    } else {
                        close(ps, rs);
                        ps = connection.prepareStatement(
                                "INSERT INTO " + mdt + " VALUES ('"
                                        + DataStore.SQLVERSION1 + "','"
                                        + DataStore.mde + "')");
                        ps.execute();
                    }

                    parent.setSqlLoaded(true);
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

    /**
     * @since 0.0.5
     */
    // Do not pull up
    @Override
    public void clearPlayerData(final PlayerData data) {
        data.setUnloading(true);
        new BukkitRunnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                final Connection connection = getConnection();
                PreparedStatement ps = null;
                final ResultSet rs = null;
                try {
                    ps = connection.prepareStatement("DELETE FROM " + pt
                            + " WHERE uuid = ?");
                    ps.setString(1, data.getPlayerId().toString());
                    ps.execute();
                    ps.close();
                    ps = connection.prepareStatement("INSERT INTO " + pt
                            + " (uuid, name) VALUES (?,?)");
                    ps.setString(1, data.getPlayerId().toString());
                    ps.setString(2, data.getPlayerName());
                    ps.execute();
                    data.clear();
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    close(ps, rs);
                    freeConnection(connection);
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

    /**
     * @since 0.0.5
     */
    @Override
    protected Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            final Iterator<Entry<Connection, Entry<Boolean, Long>>> iter = connections
                    .entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<Connection, Entry<Boolean, Long>> conn = iter
                        .next();
                if (!conn.getValue().getKey()) {
                    conn.setValue(new SimpleEntry<Boolean, Long>(true,
                            System.currentTimeMillis()));
                    return conn.getKey();
                }
            }
            final Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://" + uri + ":" + port + "/" + databaseName
                            + "?user=" + user + "&password=" + password);
            connections.put(conn, new SimpleEntry<Boolean, Long>(true,
                    System.currentTimeMillis()));
            return conn;
        } catch (final SQLException ex) {
            ex.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @since 0.0.5
     */
    @Override
    public String getStoreTypeName() {
        return "MySQL";
    }

    /**
     * @since 0.0.5
     */
    @Override
    public void freeConnection(final Connection connection) {
        connections.put(connection, new SimpleEntry<Boolean, Long>(false,
                System.currentTimeMillis()));
    }

    //    @Override
    //    public void finalizeQuests(final QuestManager manager) {
    //        final Set<UUID> uuids = manager.getIds();
    //
    //        new BukkitRunnable() {
    //            @Override
    //            public void run() {
    //                Connection connection = getConnection();
    //                PreparedStatement ps = null;
    //                ResultSet rs = null;
    //                try {
    //                    ps = connection.prepareStatement(
    //                            "SELECT * FROM " + pt + " WHERE uuid = NULL");
    //                    rs = ps.executeQuery();
    //                    ResultSetMetaData rsmd = ps.getMetaData();
    //                    LinkedHashSet<UUID> questIds = new LinkedHashSet<UUID>();
    //                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
    //                        if (rsmd.getColumnName(i).startsWith("quest")) {
    //                            questIds.add(UUID.fromString(
    //                                    rsmd.getCatalogName(i).substring(6)));
    //                        }
    //                    }
    //                    ps.close();
    //                    rs.close();
    //
    //                    int loadedQuests = 0;
    //                    Iterator<UUID> iter = uuids.iterator();
    //                    while (iter.hasNext()) {
    //                        UUID id = iter.next();
    //                        if (questIds.contains(id)) {
    //                            iter.remove();
    //                            questIds.remove(id);
    //                            loadedQuests++;
    //                        }
    //                    }
    //
    //                    Bukkit.getLogger()
    //                            .info("[RolecraftCore] Loaded " + loadedQuests
    //                                    + " quests successfully from SQL");
    //                    if (uuids.size() != 0) {
    //                        int addedQuests = 0;
    //                        iter = uuids.iterator();
    //                        StringBuilder sb = new StringBuilder(
    //                                "ALTER TABLE " + pt + " ");
    //                        while (iter.hasNext()) {
    //                            sb.append("ADD COLUMN quest:" + iter.next()
    //                                    + " VARCHAR(255) DEFAULT NULL,"); // quest's columns are quest:<UUID>
    //                            addedQuests++;
    //                        }
    //                        ps = connection.prepareStatement(
    //                                sb.substring(0, sb.length() - 1));
    //                        ps.execute();
    //                        Bukkit.getLogger().info("[RolecraftCore] Added " +
    //                                addedQuests + " quests to SQL");
    //                    }
    //
    //                    if (questIds.size() != 0) {
    //                        int deletedQuests = 0;
    //                        iter = questIds.iterator();
    //                        StringBuilder sb = new StringBuilder(
    //                                "ALTER TABLE " + pt + " ");
    //                        while (iter.hasNext()) {
    //                            sb.append("DROP COLUMN quest:" + iter.next()
    //                                    + ","); // quest's columns are quest:<UUID>
    //                            deletedQuests++;
    //                        }
    //                        ps = connection.prepareStatement(
    //                                sb.substring(0, sb.length() - 1));
    //                        ps.execute();
    //                        Bukkit.getLogger().info("[RolecraftCore] Deleted " +
    //                                deletedQuests + " obsolete quests");
    //                    }
    //
    //                    setQuestsLoaded(true);
    //                } catch (SQLException ex) {
    //                    ex.printStackTrace();
    //                } finally {
    //                    close(ps, rs);
    //                }
    //            }
    //        }.runTaskAsynchronously(getParent());
    //    }
}
