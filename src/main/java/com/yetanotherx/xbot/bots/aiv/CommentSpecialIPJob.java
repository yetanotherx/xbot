package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.util.Util;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import static com.yetanotherx.xbot.util.RegexUtil.lcMatches;

public class CommentSpecialIPJob extends BotJob<AIVBot> {

    private String page;
    private String user;
    private String mask;
    private int reportCount;

    public CommentSpecialIPJob(AIVBot bot, List<String> ipParams, int reportCount) {
        super(bot);
        this.reportCount = reportCount;
        this.page = ipParams.get(0);
        this.user = ipParams.get(1);
        this.mask = ipParams.get(2);
    }

    @Override
    public void doRun() {
        try {
            String content = bot.getParent().getWiki().getPageText(page);

            if (!content.isEmpty()) {
                List<String> newContent = new LinkedList<String>();
                boolean inComment = false;

                for (String line : content.split("\n")) {
                    inComment = Boolean.parseBoolean(AIVBot.parseComment(line, inComment)[0]);
                    
                    if (line.contains(user) && lcMatches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)", line)) {
                        if (line.contains("<!-- Marked -->")) {
                            return;
                        }

                        if (inComment) {
                            line += " -->";
                        }
                        line += " <!-- Marked -->" + "\n:*'''Note''': " + bot.getIPs().get(mask) + " ~~~~";
                        if (inComment) {
                            line += " <!--";
                        }
                    }
                    newContent.add(line);
                }

                String tally = AIVBot.getReportSummary(reportCount);
                String summary = "Commenting on " + user + ": " + bot.getIPs().get(mask);

                String newCont = Util.join("\n", newContent);
                if (!newCont.isEmpty()) {
                    this.bot.getParent().getWiki().doEdit(page, newCont, tally + summary, false);
                }
                
                XBotDebug.info("AIV", ChatColor.GOLD + user + " matched " + mask + ", marked as: " + bot.getIPs().get(mask));
            }
        } catch (IOException ex) {
            XBotDebug.error("AIV", "Could not read from wiki.", ex);
        }
    }

    @Override
    public void doShutdown() {
    }
}