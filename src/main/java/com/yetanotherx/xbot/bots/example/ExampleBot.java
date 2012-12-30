package com.yetanotherx.xbot.bots.example;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.console.ChatColor;
import static com.yetanotherx.xbot.util.Util.toLong;

public class ExampleBot extends BotThread {

    private String sandbox = "";
    
    public ExampleBot(XBot main, String name) {
        super(main, name);
    }

    @Override
    public void doRun() {
        for( ChatColor c : ChatColor.values() ) {
            XBotDebug.info(c + c.name());
        }
        //this.addJob(new CheckSandboxJob(this, toLong(0, 0, 0, 5, 0), true));
        //this.addJob(new PrintBotJob(this, toLong(0, 0, 0, 1, 0), true));
        //this.addJob(new PrintJobsJob(this, toLong(0, 0, 0, 2, 0), true));
        //this.addJob(new PrintMemoryJob(this, toLong(0, 0, 0, 3, 0), true));
        //this.addJob(new PrintThreadJob(this, toLong(0, 0, 0, 4, 0), true));
        //this.addJob(new PrintTimeJob(this, toLong(0, 0, 0, 5, 0), true));
    }

    @Override
    public void doShutdown() {
        XBotDebug.info(getRealName(), "No longer running!");
    }

    public synchronized String getSandbox() {
        return sandbox;
    }

    public synchronized void setSandbox(String sandbox) {
        if( !this.sandbox.equals(sandbox) ) {
            this.addJob(new PostResultsJob(this, 0, false));
        }
        this.sandbox = sandbox;
    }

    
}
