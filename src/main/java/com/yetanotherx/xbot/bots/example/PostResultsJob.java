package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class PostResultsJob extends BotJob<ExampleBot> {

    public PostResultsJob(ExampleBot bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        for (int i = 0; i < 5; i++) {
            try {
                String a = bot.getParent().getWiki().getPageText("User:X!/sandbox");
                a += "\n\nSandbox changed!";
                //bot.getParent().getWiki().doEdit("User:X!/Sandbox", a, "notify", true);
                XBotDebug.info("Sandbox changed");
            } catch (IOException ex) {
                Logger.getLogger(PostResultsJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void doShutdown() {
    }
}
