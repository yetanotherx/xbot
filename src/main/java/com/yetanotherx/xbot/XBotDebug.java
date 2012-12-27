package com.yetanotherx.xbot;

import com.yetanotherx.xbot.console.ChatColor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main XBot logging class.
 * 
 * @author yetanotherx
 */
public class XBotDebug {
    
    public static boolean debugMode = false;
    private final static Logger logger = Logger.getLogger("XBot");

    public static void debug(String name, String message) {
        if (debugMode) {
            if( ChatColor.isColored(message) ) {
                logger.info("[" + name + " Debug] - " + message);
            } else {
                logger.info("[" + name + " Debug] - " + ChatColor.GRAY + message);
            }
        }
    }
    
    public static void debug(String message) {
        debug("(none)", message);
    }
    
    public static void info(String name, String message) {
        logger.info("[" + name + "] - " + message);
    }
    
    public static void info(String message) {
        info("(none)", message);
    }
    
    public static void warn(String name, String message, Throwable ex) {
        if( ChatColor.isColored(message) ) {
            logger.log(Level.WARNING, "[" + name + "] - " + message, ex);
        } else {
            logger.log(Level.WARNING, "[" + name + "] - " + ChatColor.YELLOW + message, ex);
        }
    }
    
    public static void warn(String name, String message) {
        warn(name, message, null);
    }
    
    public static void warn(String message) {
        warn("(none)", message, null);
    }
    
    public static void error(String name, String message, Throwable ex) {
        if( ChatColor.isColored(message) ) {
            logger.log(Level.SEVERE, "[" + name + "] - " + message, ex);
        } else {
            logger.log(Level.SEVERE, "[" + name + "] - " + ChatColor.RED + message, ex);
        }
    }
    
    public static void error(String name, String message) {
        error(name, message, null);
    }
    
    public static void error(String message) {
        error("(none)", message, null);
    }

    public static Logger getLogger() {
        return logger;
    }

}
