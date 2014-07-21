package com.github.rolecraftdev.command.guild;

import com.github.rolecraftdev.RolecraftCore;
import org.bukkit.command.CommandSender;

public final class GuildShowCommand extends GuildSubCommand {
    public GuildShowCommand(final RolecraftCore plugin) {
        super(plugin);
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        // TODO
    }

    @Override
    public String[] getNames() {
        return new String[] { "show", "details", "info" };
    }

    @Override
    public String getPermission() {
        return "rolecraft.guild.show";
    }

    @Override
    public String getUsage() {
        return "/guild show <guild>";
    }

    @Override
    public String getDescription() {
        return "Shows information for the given guild";
    }
}
