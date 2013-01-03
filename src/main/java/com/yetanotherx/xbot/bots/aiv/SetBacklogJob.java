package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.util.Util;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static com.yetanotherx.xbot.util.RegexUtil.*;

public class SetBacklogJob extends BotJob<AIVBot> {

    private String page;
    private int reportCount;
    private Map<String, String> params;

    public SetBacklogJob(AIVBot bot, String page, int reportCount, Map<String, String> params) {
        super(bot);
        this.page = page;
        this.reportCount = reportCount;
        this.params = params;
    }

    @Override
    public void doRun() {
        try {
            int addLimit = new Integer(params.get("AddLimit"));
            int delLimit = new Integer(params.get("RemoveLimit"));

            String summary = "";

            String content = bot.getParent().getWiki().getPageText(page);
            String originalContent = content.toString();

            if (!content.isEmpty()) {
                List<String> newContent = new LinkedList<String>();
                for (String line : content.split("\n")) {

                    if (matches("^\\{\\{(?:no)?adminbacklog\\}\\}", line, Pattern.CASE_INSENSITIVE)) {
                        String tally = AIVBot.getReportSummary(reportCount);

                        if (reportCount >= addLimit) {
                            XBotDebug.info("AIV", ChatColor.PINK + "Backlog added to " + ChatColor.BLUE + page);
                            summary = tally + " Noticeboard is backlogged.";
                            line = line.replace("{{noadminbacklog", "{{adminbacklog");
                            newContent.add(line);
                        } else if (reportCount <= delLimit) {
                            XBotDebug.info("AIV", ChatColor.PINK + "Backlog removed from " + ChatColor.BLUE + page);
                            summary = tally + " Noticeboard is no longer backlogged.";
                            line = line.replace("{{adminbacklog", "{{noadminbacklog");
                            newContent.add(line);
                        }
                    } else {
                        newContent.add(line);
                    }
                }

                String newCont = Util.join("\n", newContent);
                if (!newCont.isEmpty()) {
                    if (!originalContent.equals(bot.getParent().getWiki().getPageText(page))) {
                        XBotDebug.warn("AIV", ChatColor.BLUE + page + ChatColor.YELLOW + " has changed since we read it, not changing.");
                        return;
                    } else {
                        this.bot.getParent().getWiki().doEdit(page, newCont, summary, false);
                    }
                }
            }
        } catch (IOException ex) {
            XBotDebug.error("AIV", "Could not read from wiki.", ex);
        }
    }

    @Override
    public void doShutdown() {
    }
}
