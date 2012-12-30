package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class CheckSandboxJob extends BotJob<ExampleBot> {

    public CheckSandboxJob(ExampleBot bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.info("CheckSandbox", "Getting sandbox");
        try {
            bot.setSandbox(bot.getParent().getWiki().getPageText("Wikipedia:Sandbox"));
        } catch (IOException ex) {
            Logger.getLogger(CheckSandboxJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void doShutdown() {
    }
    
}
