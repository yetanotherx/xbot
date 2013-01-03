package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.util.Util;
import java.io.IOException;

public class GetInstructionsJob extends BotJob<AIVBot> {

    public GetInstructionsJob(AIVBot bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        try {
            XBotDebug.info("AIV", ChatColor.GRAY + "Getting instructions...");

            String content = bot.getParent().getWiki().getPageText("Wikipedia:Administrator intervention against vandalism/instructions");
            if (!content.isEmpty()) {
                String inst = "";
                boolean inSection = false;

                for (String line : content.split("\n")) {
                    if (!inSection && line.startsWith("<!-- HBC AIV helperbot BEGIN INSTRUCTIONS -->")) {
                        inSection = true;
                        continue;
                    }
                    if (inSection && line.startsWith("<!-- HBC AIV helperbot END INSTRUCTIONS -->")) {
                        inSection = false;
                    }

                    if (inSection) {
                        inst += line + "\n";
                    }
                }

                bot.setInstructions(inst.trim());

                XBotDebug.debug("AIV", ChatColor.GRAY + "Instructions fetched, will recheck in " + ChatColor.BLUE + Util.millisToString(wait));
            }
        } catch (IOException ex) {
            XBotDebug.error("AIV", "Could not read from wiki.", ex);
        }
    }

    @Override
    public void doShutdown() {
    }
}
