package com.yetanotherx.xbot.console.commands;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.console.commands.util.Command;
import com.yetanotherx.xbot.console.commands.util.CommandContext;
import com.yetanotherx.xbot.exception.CommandException;
import com.yetanotherx.xbot.threads.MonitorThread;
import com.yetanotherx.xbot.util.Util;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Commands for accessing system information.
 * 
 * @author yetanotherx
 */
public class MonitorCommands extends CommandContainer {

    public MonitorCommands(XBot parent) {
        super(parent);
    }

    @Command(aliases = {"all"},
    desc = "Displays all the stats that the monitor thread knows.")
    public void all(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        XBotDebug.info("MAIN", ChatColor.YELLOW + "XBot Information");
        XBotDebug.info("MAIN", ChatColor.GRAY + " Number of bots: " + mon.getBotCount());
        XBotDebug.info("MAIN", ChatColor.GRAY + " Number of threads: " + mon.getThreadCount());
        XBotDebug.info("MAIN", ChatColor.GRAY + " Number of jobs: " + mon.getJobCount());
        XBotDebug.info("MAIN", ChatColor.GRAY + " Total memory used: " + mon.getMemory()[0]);
        XBotDebug.info("MAIN", ChatColor.GRAY + " Free memory: " + mon.getMemory()[1]);
        XBotDebug.info("MAIN", ChatColor.GRAY + " Max memory: " + mon.getMemory()[2]);
        XBotDebug.info("MAIN", ChatColor.GRAY + " Used memory: " + mon.getMemory()[3]);
        XBotDebug.info("MAIN", ChatColor.GRAY + " API calls per minute: " + mon.getApiCallsPerMinute());
        XBotDebug.info("MAIN", ChatColor.GRAY + " Uptime: " + Util.millisToString(mon.getMillisecondsRunning()));

        Map<String, String> sys = mon.getSystemInfo();
        for (Entry<String, String> entry : sys.entrySet()) {
            XBotDebug.info("MAIN", ChatColor.GRAY + " " + entry.getKey() + ": " + entry.getValue());
        }
    }

    @Command(aliases = {"bots"},
    desc = "Show all bots running.")
    public void bots(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        XBotDebug.info("MAIN", ChatColor.GOLD + " Number of bots running: " + ChatColor.BRIGHT_GREEN + mon.getBotCount());

        for (BotThread bot : parent.getBots()) {
            if (!bot.isEnabled()) {
                continue;
            }
            XBotDebug.info("MAIN", ChatColor.YELLOW + "   " + bot.getRealName() + ChatColor.CYAN + " (" + bot.getClass().getCanonicalName() + ")");
        }
    }

    @Command(aliases = {"threads"},
    desc = "Show all threads running.")
    public void threads(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        XBotDebug.info("MAIN", ChatColor.GOLD + " Number of threads running: " + ChatColor.BRIGHT_GREEN + mon.getThreadCount());

        for (Thread t : mon.getThreads()) {
            XBotDebug.info("MAIN", ChatColor.YELLOW + "   " + t.getName() + ChatColor.CYAN + " (" + t.getClass().getCanonicalName() + ")");
        }
    }

    @Command(aliases = {"jobs"},
    desc = "Show all jobs running.")
    public void jobs(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        XBotDebug.info("MAIN", ChatColor.GOLD + " Number of jobs running: " + ChatColor.BRIGHT_GREEN + mon.getJobCount());

        for (BotThread bot : parent.getBots()) {
            for (BotJob<? extends BotThread> job : bot.getJobs()) {
                if (!job.isEnabled()) {
                    continue;
                }
                StringBuilder b = new StringBuilder();
                b.append(ChatColor.YELLOW);
                b.append("   ");
                b.append(job.getClass().getCanonicalName());
                b.append(ChatColor.CYAN);
                b.append(" (");
                b.append(bot.getName());
                b.append(")");
                XBotDebug.info("MAIN", b.toString());
            }

        }
    }

    @Command(aliases = {"mem", "memory"},
    desc = "Amount of memory in use.")
    public void mem(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        XBotDebug.info("MAIN", ChatColor.GOLD + " Memory usage: ");

        XBotDebug.info("MAIN", ChatColor.YELLOW + "   Total memory: " + ChatColor.CYAN + mon.getMemory()[0] + " MB");
        XBotDebug.info("MAIN", ChatColor.YELLOW + "   Free memory: " + ChatColor.CYAN + mon.getMemory()[1] + " MB");
        XBotDebug.info("MAIN", ChatColor.YELLOW + "   Max memory: " + ChatColor.CYAN + mon.getMemory()[2] + " MB");

        ChatColor out = ChatColor.BRIGHT_GREEN;
        if (mon.getMemory()[3] > mon.getMemory()[1]) {
            out = ChatColor.YELLOW;
        }
        if (mon.getMemory()[1] < (mon.getMemory()[2] / 10)) {
            out = ChatColor.RED;
        }
        XBotDebug.info("MAIN", ChatColor.YELLOW + "   Used memory: " + out + mon.getMemory()[3] + " MB");

    }

    @Command(aliases = {"time"},
    desc = "Time since the bot was started.")
    public void time(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        XBotDebug.info("MAIN", ChatColor.YELLOW + "Uptime: " + ChatColor.CYAN + Util.millisToString(mon.getMillisecondsRunning()));
        XBotDebug.info("MAIN", ChatColor.YELLOW + "Date started: " + ChatColor.CYAN + Util.formatDate(mon.getStartTime()));
    }

    @Command(aliases = {"api"},
    desc = "Average API calls per minute.")
    public void api(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        XBotDebug.info("MAIN", ChatColor.YELLOW + "API Calls per minute: " + ChatColor.CYAN + mon.getApiCallsPerMinute());
    }

    @Command(aliases = {"info"},
    desc = "System info.")
    public void info(CommandContext args) throws CommandException {
        MonitorThread mon = parent.getMonitor();

        Map<String, String> sys = mon.getSystemInfo();
        for (Entry<String, String> entry : sys.entrySet()) {
            XBotDebug.info("MAIN", ChatColor.YELLOW + " " + entry.getKey() + ": " + ChatColor.CYAN + entry.getValue());
        }
    }
}
