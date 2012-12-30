package com.yetanotherx.xbot.console.commands;

import com.google.common.base.Joiner;
import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.console.commands.util.Command;
import com.yetanotherx.xbot.console.commands.util.CommandContext;
import com.yetanotherx.xbot.console.commands.util.NestedCommand;
import com.yetanotherx.xbot.exception.CommandException;

/**
 * Core XBot commands
 * 
 * @author yetanotherx
 */
public class RootCommands extends CommandContainer {

    public RootCommands(XBot parent) {
        super(parent);
    }

    @Command(aliases = {"help"},
    usage = "[<command>]",
    desc = "Displays help for the given command or lists all commands.",
    min = 0,
    max = 1)
    public void help(CommandContext args) throws CommandException {

        if (args.argsLength() == 0) {
            XBotDebug.info("MAIN", ChatColor.GRAY + "Commands: " + Joiner.on(", ").join(parent.getAllCommands()));
            return;
        }

        String command = args.getString(0).replaceAll("/", "");

        String helpMessage = parent.getCommandManager().getHelpMessages().get(command);
        if (helpMessage == null) {
            throw new CommandException("Unknown command '" + command + "'.");
        }

        XBotDebug.info("MAIN", ChatColor.GRAY + helpMessage);
    }

    @Command(aliases = {"version", "ver"},
    desc = "Gets the current XBot version")
    public void version(CommandContext args) throws CommandException {
        XBotDebug.info("MAIN", ChatColor.GRAY + "Current XBot version: " + XBot.getVersion());
    }

    @Command(aliases = {"stop", "exit"},
    desc = "Stops the bot")
    public void stop(CommandContext args) throws CommandException {
        XBotDebug.info("MAIN", ChatColor.RED + "Stopping XBot from command");
        System.exit(0);
    }

    @Command(aliases = {"halt", "disable-all"},
    desc = "Stops all bot processes. Should always do this before stop.")
    public void halt(CommandContext args) throws CommandException {
        XBotDebug.info("MAIN", ChatColor.RED + "Stopping all bot processes");
        for (BotThread bot : this.parent.getBots()) {
            if (bot.isEnabled()) {
                parent.disableBot(bot);
            }
        }
    }

    @Command(aliases = {"disable"},
    usage = "[<bot_name>]",
    desc = "Disables the given bot, if it was enabled.",
    min = 1,
    max = 1)
    public void disable(CommandContext args) throws CommandException {
        String bot = args.getString(0);

        if (parent.isBotEnabled(bot)) {
            XBotDebug.info("MAIN", ChatColor.BRIGHT_GREEN + "Disabling bot " + bot);
            parent.disableBot(bot);
        } else {
            XBotDebug.error("MAIN", ChatColor.RED + bot + " is not running! ");
        }
    }

    @Command(aliases = {"enable"},
    usage = "[<bot_name>]",
    desc = "Starts a new bot with the given name",
    min = 1,
    max = 1)
    public void enable(CommandContext args) throws CommandException {
        String bot = args.getString(0);
        if (parent.isBotEnabled(bot)) {
            XBotDebug.error("MAIN", bot + " is already running!");
        } else {
            parent.enableBot(bot);
            XBotDebug.info("MAIN", ChatColor.BRIGHT_GREEN + bot + " is now running!");
        }
    }

    @Command(aliases = {"monitor", "sys"},
    desc = "Monitor commands")
    @NestedCommand(MonitorCommands.class)
    public void sys(CommandContext args) throws CommandException {
    }
}
