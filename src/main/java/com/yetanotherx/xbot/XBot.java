package com.yetanotherx.xbot;

import com.yetanotherx.xbot.threads.MonitorThread;
import com.yetanotherx.xbot.threads.ServerShutdownThread;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.bots.example.ExampleBot;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.console.ConsoleManager;
import com.yetanotherx.xbot.console.commands.RootCommands;
import com.yetanotherx.xbot.console.commands.util.CommandManager;
import com.yetanotherx.xbot.exception.CommandException;
import com.yetanotherx.xbot.exception.CommandUsageException;
import com.yetanotherx.xbot.exception.MissingNestedCommandException;
import com.yetanotherx.xbot.exception.UnhandledCommandException;
import com.yetanotherx.xbot.exception.WrappedCommandException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.sourceforge.jwbf.core.actions.HttpActionClient;

/**
 * Controller for XBot. Manages various Bot threads, console, commands, etc.
 * 
 * TODO: Use events
 * TODO: Don't hardcode bots
 * 
 * @author yetanotherx
 */
public class XBot {

    /**
     * Version of XBot. Set in getVersion()
     */
    private static String version;
    
    /**
     * All the registered bots. Key is the name, it must match the constructor name of the bot.
     */
    private final Map<String, BotThread> bots = Collections.synchronizedMap(new HashMap<String, BotThread>());
    
    /**
     * Spout's console manager instance
     */
    private final ConsoleManager consoleManager;
    
    /**
     * sk89q's command manager instance
     */
    private final CommandManager commandManager;
    
    /**
     * Monitor thread instance.
     */
    private final MonitorThread monitor;
    
    /**
     * Command line arguments. Direct copy of args in the main method
     */
    private final String[] args;
    
    /**
     * Configuration from config & command line
     */
    private final XBotConfig conf;
    
    /**
     * Main wiki instance
     */
    private Wiki wiki;
    
    static {
        getVersion();   // Sets version variable
    }

    public XBot(String[] args, XBotConfig conf, boolean useJline, boolean useConsole) {
        this.conf = conf;
        this.args = args;

        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));

        this.consoleManager = new ConsoleManager(this, useJline, useConsole);
        this.consoleManager.start();

        this.wiki = this.makeWiki();
        this.monitor = new MonitorThread(this);

        this.commandManager = new CommandManager(this);

        this.addCommands();
        this.addBots();
    }

    /**
     * Starts the actual bot. Starts the monitor & wiki, and then
     * individually starts each bot thread.
     * @throws InterruptedException 
     */
    public void beginRunningBot() throws InterruptedException {
        XBotDebug.debug("MAIN", "Starting monitor thread.");
        monitor.start();
        wiki.begin();

        for (Entry<String, BotThread> bot : bots.entrySet()) {
            XBotDebug.info("MAIN", "Starting bot " + bot.getKey());
            bot.getValue().start();
            Thread.sleep(50);
        }

        while (true) {
            // Let the program run. If no bots are left, program still runs
        }
    }

    /**
     * Makes a wiki instance, using the custom HttpActionClient which throttles
     * outputs. 
     * @return 
     */
    private Wiki makeWiki() {
        try {
            XBotDebug.debug("MAIN", "Creating Wiki instance...");
            HttpActionClient client = new HttpActionClientThrottled(this, new URL(conf.getURL()));
            return new Wiki(this, client);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Malformed URL given", ex);
        }
    }

    /**
     * Registers all bot threads. 
     */
    private synchronized void addBots() {
        XBotDebug.debug("MAIN", "Registering bots");
        
        // Key MUST MATCH the constructor's name!
        
        //bots.put("AIV", new AIVBot(this, "AIV"));
        bots.put("Example", new ExampleBot(this, "Example"));
    }

    /**
     * Registers all command classes
     */
    private void addCommands() {
        XBotDebug.debug("MAIN", "Registering commands");
        commandManager.register(RootCommands.class);
    }

    /**
     * Disables the given bot
     * @param bot 
     */
    public void disableBot(BotThread bot) {
        XBotDebug.debug("MAIN", "Disabling bot " + bot.getName());
        bot.disable();
    }

    /**
     * Disables the bot with the given name
     * @param botName 
     */
    public synchronized void disableBot(String botName) {
        this.disableBot(bots.get(botName));
    }

    public String[] getArgs() {
        return args;
    }

    public synchronized List<BotThread> getBotList() {
        return new ArrayList<BotThread>(bots.values());
    }

    public synchronized Map<String, BotThread> getBots() {
        return bots;
    }

    public XBotConfig getConf() {
        return conf;
    }

    public MonitorThread getMonitor() {
        return monitor;
    }

    public Wiki getWiki() {
        return wiki;
    }

    public ConsoleManager getConsole() {
        return this.consoleManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Parses console input, which is assumed to be a command.
     * @param trim 
     */
    public void processConsoleInput(String trim) {
        new CommandThread(trim).start();
    }

    public String[] getAllCommands() {
        return this.commandManager.getCommands().keySet().toArray(new String[0]);
    }

    private class CommandThread extends Thread {

        public String line;

        public CommandThread(String line) {
            this.line = line;
        }

        @Override
        public void run() {
            String[] split = line.split(" ");
            if (split.length < 1) {
                return;
            }

            try {
                try {
                    if (!commandManager.hasCommand(split[0])) {
                        XBotDebug.error("MAIN", "Unknown command: " + split[0]);
                    } else {
                        commandManager.execute(split);
                    }
                } catch (MissingNestedCommandException e) {
                    XBotDebug.error("MAIN", ChatColor.RED + e.getUsage());
                } catch (CommandUsageException e) {
                    XBotDebug.error("MAIN", ChatColor.RED + e.getMessage());
                    XBotDebug.error("MAIN", ChatColor.RED + e.getUsage());
                } catch (UnhandledCommandException e) {
                    return;
                } catch (WrappedCommandException e) {
                    throw e.getCause();
                } catch (CommandException e) {
                    XBotDebug.error("MAIN", ChatColor.RED + e.getMessage());
                } catch (NumberFormatException e) {
                    XBotDebug.error("MAIN", ChatColor.RED + "Number expected; string given.");
                }
            } catch (Throwable e) {
                XBotDebug.error("MAIN", ChatColor.RED + "Unknown exception", e);
            }
        }
    }

    /**
     * Gets version from the package / META-INF
     * @return 
     */
    public static String getVersion() {
        if (version != null) {
            return version;
        }

        Package p = XBot.class.getPackage();

        if (p == null) {
            p = Package.getPackage("com.yetanotherx.xbot");
        }

        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();

            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }
}
