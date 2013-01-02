package com.yetanotherx.xbot;

import com.yetanotherx.xbot.bots.BotRegistration;
import com.yetanotherx.xbot.threads.MonitorThread;
import com.yetanotherx.xbot.threads.ServerShutdownThread;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.console.ConsoleManager;
import com.yetanotherx.xbot.console.commands.RootCommands;
import com.yetanotherx.xbot.console.commands.WikiCommands;
import com.yetanotherx.xbot.console.commands.util.CommandManager;
import com.yetanotherx.xbot.exception.CommandException;
import com.yetanotherx.xbot.exception.CommandUsageException;
import com.yetanotherx.xbot.exception.MissingNestedCommandException;
import com.yetanotherx.xbot.exception.UnhandledCommandException;
import com.yetanotherx.xbot.exception.WrappedCommandException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

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
     * All the registered bots. 
     */
    private final List<BotThread> bots = Collections.synchronizedList(new ArrayList<BotThread>());
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
    private XBotWiki wiki;

    static {
        getVersion();   // Sets version variable
    }

    public XBot(String[] args, XBotConfig conf, boolean useJline, boolean useConsole) {
        this.conf = conf;
        this.args = args;

        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));

        this.consoleManager = new ConsoleManager(this, useJline, useConsole);
        this.consoleManager.start();

        this.wiki = new XBotWiki(this, conf.getURL());
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

        if (!this.conf.getOptions().has("nobots")) {
            for (BotThread bot : bots) {
                XBotDebug.info("MAIN", ChatColor.BRIGHT_GREEN + "Starting bot " + bot.getRealName());
                bot.start();
                Thread.sleep(50);
            }
        } else {
            XBotDebug.warn("MAIN", "Not starting any bots. Bots must be enabled manually.");
            
        }

        while (true) {
            // Let the program run. If no bots are left, program still runs
        }
    }

    /**
     * Registers all bot threads. 
     */
    private synchronized void addBots() {
        if (this.conf.getOptions().has("nobots")) {
            return;
        }
        
        XBotDebug.debug("MAIN", "Registering bots");

        // Key MUST MATCH the constructor's name!

        //bots.put("AIV", new AIVBot(this, "AIV"));
        //bots.put("Example", new ExampleBot(this, "Example"));

        for (Entry<String, Class<? extends BotThread>> b : BotRegistration.botList.entrySet()) {
            try {
                bots.add(b.getValue().getConstructor(XBot.class, String.class).newInstance(this, b.getKey()));
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }

    /**
     * Registers all command classes
     */
    private void addCommands() {
        XBotDebug.debug("MAIN", "Registering commands");
        commandManager.register(RootCommands.class);
        commandManager.register(WikiCommands.class);

    }

    /**
     * Disables the given bot
     * @param bot 
     */
    public void disableBot(BotThread bot) {
        XBotDebug.debug("MAIN", "Disabling bot " + bot.getRealName());
        bot.disable();
    }

    /**
     * Disables the bot with the given name
     * @param botName 
     */
    public synchronized void disableBot(String botName) {
        for (BotThread bot : bots) {
            if (botName.equals(bot.getRealName()) && bot.isEnabled()) {
                this.disableBot(bot);
                return;
            }
        }
    }

    /**
     * Enables the bot with the given name
     * @param botName 
     */
    public synchronized void enableBot(String botName) {
        Class<? extends BotThread> clazz = BotRegistration.botList.get(botName);
        if (clazz != null) {
            XBotDebug.debug("MAIN", "Enabling bot " + botName);

            try {
                BotThread bot = clazz.getConstructor(XBot.class, String.class).newInstance(this, botName);
                bots.add(bot);
                bot.start();
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }

    public boolean isBotEnabled(String botName) {
        for (BotThread bot : bots) {
            if (botName.equals(bot.getRealName())) {
                return bot.isEnabled();
            }
        }
        return false;
    }

    public String[] getArgs() {
        return args;
    }

    public synchronized List<BotThread> getBots() {
        return bots;
    }

    public XBotConfig getConf() {
        return conf;
    }

    public MonitorThread getMonitor() {
        return monitor;
    }

    public XBotWiki getWiki() {
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
