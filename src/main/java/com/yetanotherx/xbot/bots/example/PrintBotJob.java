package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;

public class PrintBotJob extends BotJob {

    public PrintBotJob(BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.info(bot.getName(), "Bots: " + bot.getParent().getMonitor().getBotCount());
    }

    @Override
    public void doShutdown() {
        XBotDebug.info(bot.getName(), "Final bot count: " + bot.getParent().getMonitor().getBotCount());
    }
    
}
