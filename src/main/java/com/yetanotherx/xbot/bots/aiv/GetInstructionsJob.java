package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.bots.BotJob;

public class GetInstructionsJob extends BotJob {

    public GetInstructionsJob(BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.debug("AIV", "Getting instructions...");
        AIVBot aivbot = (AIVBot) bot;
        
        String content = aivbot.getParent().getWiki().readData("Wikipedia:Administrator intervention against vandalism/instructions").getText();
        if( !content.isEmpty() ) {
            String inst = "";
            boolean inSec = false;
            for( String line : content.split("\n") ) {
                if( !inSec && line.startsWith("<!-- HBC AIV helperbot BEGIN INSTRUCTIONS -->") ) {
                    inSec = true;
                    continue;
                }
                if( inSec && line.startsWith("<!-- HBC AIV helperbot END INSTRUCTIONS -->") ) {
                    inSec = false;
                }
                
                if( !inSec ) {
                    inst += line + "\n";
                }
            }
            
            aivbot.setInstructions(inst.trim());
            
            XBotDebug.debug("AIV", "Instructions fetched, will recheck later.");
        }
    }

    @Override
    public void doShutdown() {
    }
    
}
