package com.yetanotherx.xbot;

/**
 * Main XBot class. Loads configuration and detects whether to use jline
 * 
 * @author yetanotherx
 */
public class XBotMain {

    public static void main(String[] args) {
        
        XBotConfig conf = XBotConfig.parseArguments(args);
        
        if (conf.getOptions().has("v")) {
            System.out.println("XBot Version - " + XBot.getVersion());
        } else {
            try {
                boolean useJline = !System.getProperty("jline.terminal", "").equals("jline.UnsupportedTerminal");

                if (conf.getOptions().has("nojline")) {
                    System.setProperty("user.language", "en");
                    useJline = false;
                }

                if (!useJline) {
                    System.setProperty("jline.terminal", jline.UnsupportedTerminal.class.getName());
                }

                boolean useConsole = true;
                if (conf.getOptions().has("noconsole")) {
                    useConsole = false;
                }

                XBot bot = new XBot(args, conf, useJline, useConsole);
                bot.beginRunningBot();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

}
