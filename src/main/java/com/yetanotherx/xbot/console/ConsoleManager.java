package com.yetanotherx.xbot.console;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.NullCompletor;
import jline.SimpleCompletor;

/**
 * Console manager. Takes care of file logging, jline, command input, etc.
 * 
 * @author Spout
 */
public class ConsoleManager {

    private final XBot parent;
    private ConsoleReader reader;
    private ConsoleCommandThread thread;
    private final FancyConsoleHandler consoleHandler;
    private final RotatingFileHandler fileHandler;
    private boolean running = true;
    private boolean jline = true;
    private boolean console = true;
    private PrintStream oldOut;

    public ConsoleManager(XBot parent, boolean useJline, boolean useConsole) {
        this.parent = parent;

        jline = useJline;
        console = useConsole;

        consoleHandler = new FancyConsoleHandler();
        
        String logFile = parent.getConf().getOptions().valueOf("log-file").toString();
        if (new File(logFile).getParentFile() != null) {
            new File(logFile).getParentFile().mkdirs();
        }
        
        fileHandler = new RotatingFileHandler(logFile);

        consoleHandler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("HH:mm:ss")));
        fileHandler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")));

        Logger logger = Logger.getLogger("");
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);

        try {
            reader = new ConsoleReader();
        } catch (IOException ex) {
            XBotDebug.error("MAIN", "Exception inintializing console reader", ex);
            ex.printStackTrace();
        }

        oldOut = new PrintStream(System.out);   // Save for stop() method;
        System.setOut(new PrintStream(new LoggerOutputStream(Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(Level.SEVERE), true));

        XBotDebug.debug("MAIN", "Console initialized.");
    }

    /**
     * Returns the original System.out
     * @return 
     */
    public PrintStream getOldOut() {
        return oldOut;
    }
    
    public void start() {
        thread = new ConsoleCommandThread();
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Disables the console. Stops all the handlers, as well as 
     * printing a newline
     */
    public void disable() {
        running = false;
        consoleHandler.flush();
        fileHandler.flush();
        fileHandler.close();
        
        // Forces a newline
        // Ideally, this will be the absolute last statement executed.
        jline = false;
        oldOut.println();
    }

    /**
     * Adds all the commands to the completer, which allows tab-complete
     */
    public void refreshCommands() {
        for (Object c : reader.getCompletors()) {
            reader.removeCompletor((Completor) c);
        }

        Completor[] list = new Completor[2];
        list[0] = new SimpleCompletor(parent.getAllCommands());
        list[1] = new NullCompletor();

        reader.addCompletor(new ArgumentCompletor(list));
    }

    /**
     * Replaces a ChatColor control sequence with an ANSI control sequence.
     * @param string
     * @return 
     */
    public String colorize(String string) {
        if (!string.contains("\u00A7")) {
            return string;
        } else if (!jline || !reader.getTerminal().isANSISupported()) {
            return ChatColor.strip(string);
        } else {
            return string.replace(ChatColor.RED.toString(), "\033[1;31m").replace(ChatColor.YELLOW.toString(), "\033[1;33m").replace(ChatColor.BRIGHT_GREEN.toString(), "\033[1;32m").replace(ChatColor.CYAN.toString(), "\033[1;36m").replace(ChatColor.BLUE.toString(), "\033[1;34m").replace(ChatColor.PINK.toString(), "\033[1;35m").replace(ChatColor.BLACK.toString(), "\033[0;0m").replace(ChatColor.DARK_GRAY.toString(), "\033[1;30m").replace(ChatColor.DARK_RED.toString(), "\033[0;31m").replace(ChatColor.GOLD.toString(), "\033[0;33m").replace(ChatColor.DARK_GREEN.toString(), "\033[0;32m").replace(ChatColor.DARK_CYAN.toString(), "\033[0;36m").replace(ChatColor.DARK_BLUE.toString(), "\033[0;34m").replace(ChatColor.PURPLE.toString(), "\033[0;35m").replace(ChatColor.GRAY.toString(), "\033[0;37m").replace(ChatColor.WHITE.toString(), "\033[1;37m") + "\033[0m";
        }
    }

    /**
     * Thread which handles console input. 
     */
    private class ConsoleCommandThread extends Thread {

        @Override
        public void run() {
            String command;
            while (running && console) {
                try {
                    if (jline) {
                        command = reader.readLine(">", null);
                    } else {
                        command = reader.readLine();
                    }

                    if (command == null || command.trim().length() == 0) {
                        continue;
                    }

                    parent.processConsoleInput(command.trim());
                } catch (Exception ex) {
                    XBotDebug.error("MAIN", "Impossible exception while executing command", ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    private class LoggerOutputStream extends ByteArrayOutputStream {

        private final String separator = System.getProperty("line.separator");
        private final Level level;

        public LoggerOutputStream(Level level) {
            super();
            this.level = level;
        }

        @Override
        public synchronized void flush() throws IOException {
            super.flush();
            String record = this.toString();
            super.reset();

            if (record.length() > 0 && !record.equals(separator)) {
                XBotDebug.getLogger().logp(level, "LoggerOutputStream", "log" + level, record);
            }
        }
    }

    private class FancyConsoleHandler extends ConsoleHandler {

        @Override
        public synchronized void flush() {
            try {
                if (jline && console) {
                    reader.printString(ConsoleReader.RESET_LINE + "");
                    reader.flushConsole();
                    super.flush();
                    try {
                        reader.drawLine();
                    } catch (Exception ex) {
                        reader.getCursorBuffer().clearBuffer();
                    }
                    reader.flushConsole();
                } else {
                    super.flush();
                }
            } catch (IOException ex) {
                XBotDebug.error("MAIN", "I/O exception flushing console output", ex);
            }
        }
    }

    private class RotatingFileHandler extends StreamHandler {

        private final SimpleDateFormat date;
        private final String logFile;
        private String filename;

        public RotatingFileHandler(String logFile) {
            this.logFile = logFile;
            date = new SimpleDateFormat("yyyy-MM-dd");
            filename = calculateFilename();

            try {
                if ((Boolean) parent.getConf().getOptions().valueOf("enable-logging")) {
                    setOutputStream(new FileOutputStream(filename, true));
                }
            } catch (FileNotFoundException ex) {
                XBotDebug.error("MAIN", "Unable to open " + filename, ex);
            }
        }

        @Override
        public synchronized void flush() {
            if (!filename.equals(calculateFilename())) {
                filename = calculateFilename();
                XBotDebug.info("MAIN", "Log rotating to " + filename);
                try {
                    setOutputStream(new FileOutputStream(filename, true));
                } catch (FileNotFoundException ex) {
                    XBotDebug.error("MAIN", "Unable to open " + filename, ex);
                }
            }
            super.flush();
        }

        private String calculateFilename() {
            return logFile.replace("%D", date.format(new Date()));
        }
    }

    private class DateOutputFormatter extends Formatter {

        private final SimpleDateFormat date;

        public DateOutputFormatter(SimpleDateFormat date) {
            this.date = date;
        }

        @Override
        public String format(LogRecord record) {
            if (!console) {
                return "";
            }
            StringBuilder builder = new StringBuilder();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(colorize(formatMessage(record).trim()));
            builder.append('\n');

            if (record.getThrown() != null) {
                StringWriter writer = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(writer));
                builder.append(writer.toString());
            }

            return builder.toString();
        }
    }

}