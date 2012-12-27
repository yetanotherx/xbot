package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;

public class PrintJobsJob extends BotJob {

    public PrintJobsJob(BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.info(bot.getName(), "Jobs: " + bot.getParent().getMonitor().getJobCount());
    }

    @Override
    public void doShutdown() {
        XBotDebug.info(bot.getName(), "Final job count: " + bot.getParent().getMonitor().getJobCount());
    }
    
}
