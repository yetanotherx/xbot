package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;

public class PrintTimeJob extends BotJob {

    public PrintTimeJob(BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.info(bot.getName(), "Time: " + bot.getParent().getMonitor().getMillisecondsRunning());
    }

    @Override
    public void doShutdown() {
        XBotDebug.info(bot.getName(), "Final time: " + bot.getParent().getMonitor().getMillisecondsRunning());
    }
    
}
