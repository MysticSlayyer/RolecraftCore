package com.github.rolecraftdev.guild;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a player rank within a specific guild. Ranks can be created by the
 * guild leader and can have different permissions
 */
public final class GuildRank {
    /**
     * The name of this guild rank
     */
    private final String name;
    /**
     * The actions within the guild which members of this guild rank are allowed
     * to perform
     */
    private final Set<GuildAction> permitted;
    /**
     * All of the members of the guild this guild rank is included in who are
     * in this rank
     */
    private final Set<UUID> members;

    /**
     * Creates a new guild rank using the given parameters
     *
     * @param name      The name of the new guild rank
     * @param permitted The actions the new rank's members can perform
     * @param members   The members of the new guild rank
     */
    public GuildRank(final String name, final Set<GuildAction> permitted,
            final Set<UUID> members) {
        this.name = name;
        this.permitted = permitted;
        this.members = members;
    }

    /**
     * Gets the name of this guild rank
     *
     * @return The name of this guild rank
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a copy of the set used to hold the guild actions members of this
     * guild rank are allowed to perform
     *
     * @return A copy of the Set containing guild actions members of this rank
     * can perform
     */
    public Set<GuildAction> getPermittedActions() {
        return new HashSet<GuildAction>(permitted);
    }

    /**
     * Gets a copy of the set used to hold the members of the guild who are a
     * part of this guild rank
     *
     * @return A copy of the Set of members of the guild who are part of this
     * guild rank
     */
    public Set<UUID> getMembers() {
        return new HashSet<UUID>(members);
    }

    /**
     * Checks whether the given player is a part of this guild rank
     *
     * @param player The player whose membership of this rank is in question
     * @return true if this rank contains the given player, otherwise false
     */
    public boolean hasPlayer(final UUID player) {
        return members.contains(player);
    }

    /**
     * Checks whether the given guild action can be performed by a member of
     * this rank - i.e whether it is included in this rank's set of permitted
     * guild actions
     *
     * @param action The action whose inclusion in the permitted actions of this
     *               rank is being checked
     * @return Whether the given action can be performed by members of this rank
     */
    public boolean can(final GuildAction action) {
        return permitted.contains(action);
    }

    /**
     * Adds the given player as a member of this guild rank
     *
     * @param member The unique identifier of the player to add to this rank
     */
    public void addMember(final UUID member) {
        members.add(member);
    }

    /**
     * Removes the given player from being a member of this guild rank
     *
     * @param member The unique identifier of the player to remove from this
     *               rank
     */
    public void removeMember(final UUID member) {
        members.remove(member);
    }

    /**
     * Creates a String for storage in SQL, does not use commas
     *
     * @return
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":");
        for (GuildAction action : permitted) {
            sb.append(action.ordinal());
            sb.append("#");
        }
        sb = new StringBuilder(sb.substring(0, sb.length() - 2));
        sb.append(":");
        for (UUID id : members) {
            sb.append(id.toString());
            sb.append("#");
        }
        return sb.substring(0, sb.length() - 2);
    }

    /**
     * Deserializes the given String into a GuildRank object, assuming it is
     * a valid serialized GuildRank. Can be built in a separate thread and
     * passed into main, assuming the other thread destroys all references
     *
     * @param s The String to deserialize into a GuildRank object
     * @return
     */
    public static GuildRank deserialize(String s) {
        String[] data = s.split(":");
        Set<GuildAction> actions = new HashSet<GuildAction>();
        Set<UUID> members = new HashSet<UUID>();
        for (String action : data[1].split("#")) {
            int actionValue = Integer.parseInt(action);
            actions.add(GuildAction.values()[actionValue]);
        }
        for (String member : data[2].split("#")) {
            members.add(UUID.fromString(member));
        }

        return new GuildRank(data[0], actions, members);
    }
}
