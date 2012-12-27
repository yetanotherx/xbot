package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;

public class PrintThreadJob extends BotJob {

    public PrintThreadJob(BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.info(bot.getName(), "Threads: " + bot.getParent().getMonitor().getThreadCount());
        Thread[] list = bot.getParent().getMonitor().getThreads();
        for( Thread t : list ) {
            XBotDebug.warn(t.getClass().getName());
        }
    }

    @Override
    public void doShutdown() {
        XBotDebug.info(bot.getName(), "Final threads: " + bot.getParent().getMonitor().getThreadCount());
        
    }
    
}
