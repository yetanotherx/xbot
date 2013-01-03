package com.yetanotherx.xbot.threads;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotThread;

public class ServerShutdownThread extends Thread {

    private XBot main;

    public ServerShutdownThread(XBot main) {
        this.main = main;
    }

    public void run() {
        XBotDebug.info("MAIN", "Shutting down bot threads.");

        for (BotThread bot : main.getBots()) {
            if (!bot.isEnabled()) {
                continue;
            }
            XBotDebug.info("MAIN", "Shutting down " + bot.getClass().getSimpleName());
            bot.disable();
        }

        main.getWiki().shutdown();

        main.getMonitor().disable();
        main.getConsole().disable();
    }
}
