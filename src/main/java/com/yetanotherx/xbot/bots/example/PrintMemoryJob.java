package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;

public class PrintMemoryJob extends BotJob {

    public PrintMemoryJob(BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.info(bot.getName(), "Total Memory: " + bot.getParent().getMonitor().getMemory()[0]);
        XBotDebug.info(bot.getName(), "Free Memory: " + bot.getParent().getMonitor().getMemory()[1]);
        XBotDebug.info(bot.getName(), "Max Memory: " + bot.getParent().getMonitor().getMemory()[2]);
    }

    @Override
    public void doShutdown() {
        XBotDebug.info(bot.getName(), "Fianl Total Memory: " + bot.getParent().getMonitor().getMemory()[0]);
        XBotDebug.info(bot.getName(), "Final Free Memory: " + bot.getParent().getMonitor().getMemory()[1]);
        XBotDebug.info(bot.getName(), "Final Max Memory: " + bot.getParent().getMonitor().getMemory()[2]);
    }
    
}
