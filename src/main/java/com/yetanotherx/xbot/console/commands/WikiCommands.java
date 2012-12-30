package com.yetanotherx.xbot.console.commands;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.XBotWiki.Edit;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.console.commands.util.Command;
import com.yetanotherx.xbot.console.commands.util.CommandContext;
import com.yetanotherx.xbot.exception.CommandException;

/**
 * XBot Wiki commands
 * 
 * @author yetanotherx
 */
public class WikiCommands extends CommandContainer {

    public WikiCommands(XBot parent) {
        super(parent);
    }

    @Command(aliases = {"flush"},
    desc = "Flush the article write queue.")
    public void flush(CommandContext args) throws CommandException {
        parent.getWiki().writeAllPending();
        XBotDebug.info("MAIN", ChatColor.GRAY + "Post queue flushed.");
    }

    @Command(aliases = {"show-queue"},
    desc = "Shows the contents of the post queue")
    public void showQueue(CommandContext args) throws CommandException {
        XBotDebug.info("MAIN", ChatColor.GOLD + " Current pending article writes: " + ChatColor.BRIGHT_GREEN + parent.getWiki().getPending().size());
        XBotDebug.info("MAIN", ChatColor.GOLD + "Post throttling is currently " + (parent.getWiki().doEnableWriteThrottling() ? "enabled" : "disabled"));
        
        for (Edit a : parent.getWiki().getPending()) {
            XBotDebug.info("MAIN", ChatColor.YELLOW + "   " + a.title);
        }
    }

    @Command(aliases = {"queue"},
    desc = "Enable/Disable the post queue")
    public void queue(CommandContext args) throws CommandException {
        parent.getWiki().setEnableWriteThrottling(!parent.getWiki().doEnableWriteThrottling());
        XBotDebug.info("MAIN", ChatColor.GOLD + "Post throttling is now " + (parent.getWiki().doEnableWriteThrottling() ? "enabled" : "disabled"));
    }

}
