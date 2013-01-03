package com.yetanotherx.xbot.console.commands;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
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

    @Command(aliases = {"queue"},
    desc = "Enable/Disable the post queue")
    public void queue(CommandContext args) throws CommandException {
        parent.getWiki().setEnableWriteThrottling(!parent.getWiki().doEnableWriteThrottling());
        XBotDebug.info("MAIN", ChatColor.GOLD + "Post throttling is now " + (parent.getWiki().doEnableWriteThrottling() ? "enabled" : "disabled"));
    }

}
