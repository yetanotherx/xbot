package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.wiki.Edit;
import java.util.Calendar;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.console.ChatColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.yetanotherx.xbot.util.RegexUtil.*;

public class FixInstructionsJob extends BotJob<AIVBot> {

    private String page;

    public FixInstructionsJob(AIVBot bot, String page) {
        super(bot);
        this.page = page;
    }

    @Override
    public void doRun() {
        try {
            XBotDebug.info("AIV", ChatColor.GRAY + "Fixing instructions...");

            String summary = "";
            String content = bot.getParent().getWiki().getPageText(page);
            String originalContent = content.toString();
            Calendar time = bot.getParent().getWiki().getTimestamp();

            if (!content.isEmpty()) {
                if (matches("===\\s*User-reported\\s*===", content)) {
                    content = content.replaceAll("<!-- HagermanBot Auto-Unsigned -->", "RE-ADD-HAGERMAN"); // hell if I know...

                    List<String> reportsToMove = new ArrayList<String>();
                    boolean inComment = false;
                    int reportCount = 0;
                    String message = "";

                    for (String line : content.split("\n")) {
                        String[] parsed = AIVBot.parseComment(line, inComment);

                        inComment = Boolean.parseBoolean(parsed[0]);
                        String remainder = parsed[2];

                        String reg = "\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(?!(?:IP ?address|username))";
                        if (matches(reg, line, Pattern.CASE_INSENSITIVE)) {
                            if (inComment) {
                                // Something other than the example template's in the instructions
                                // Gonna move it later
                                reportsToMove.add(line);
                                reportCount++;
                            }
                        } else if (matches(reg, remainder, Pattern.CASE_INSENSITIVE)) {
                            remainder = remainder.replace("-->", "");
                            reportsToMove.add(remainder);
                        }
                    }

                    if (matches("===\\s*User-reported\\s*===\\s+<!--", content, Pattern.DOTALL)) {
                        Matcher m = getMatcher("(===\\s*User-reported\\s*===\\s+)<!--.*?(-->|$)", content, Pattern.DOTALL);
                        if (m.find()) {
                            content = m.replaceAll(m.group(1) + bot.getInstructions());
                            message = "";
                        }
                    } else {
                        Matcher m = getMatcher("(===\\s*User-reported\\s*===\\n)", content, Pattern.DOTALL);
                        if (m.find()) {
                            content = m.replaceAll(m.group(1) + bot.getInstructions() + "\n");
                            message = " Old instructions not found, please check page for problems.";
                        }
                    }

                    String remainingText = AIVBot.getReportSummary(reportCount);

                    if (!reportsToMove.isEmpty()) {
                        if (reportsToMove.size() > 50) {
                            summary = remainingText + " Reset [[WP:AIV/I|instruction block]], WARNING: tried to move more than 50 reports, aborting - check history for lost reports." + message;
                        } else {
                            for (String report : reportsToMove) {
                                if (report.contains("RE-ADD-HAGERMAN")) {
                                    report.replaceAll("RE-ADD-HAGERMAN", "<!-- HagermanBot Auto-Unsigned -->");
                                    report.replaceAll("~~~~", "");
                                } else {
                                    report.replaceAll("~~~~", "~~~~ <small><sup>(Original signature lost - report made inside comment)</sup></small>");
                                }
                                content += report + "\n";
                            }
                            summary = remainingText + " Reset [[WP:AIV/I|instruction block]], " + reportsToMove.size() + " report(s) moved to end of page." + message;
                        }
                    } else {
                        summary = remainingText + " Reset [[WP:AIV/I|instruction block]]." + message;
                    }

                    content.replaceAll("RE-ADD-HAGERMAN", "<!-- HagermanBot Auto-Unsigned -->");
                    if (!originalContent.equals(bot.getParent().getWiki().getPageText(page))) {
                        XBotDebug.warn("AIV", ChatColor.BLUE + page + ChatColor.YELLOW + " has changed since we read it, not changing.");
                        return;
                    } else {
                        new Edit(page, content, summary, time).run(bot.getParent(), bot.getRunPage());
                    }
                    XBotDebug.info("AIV", ChatColor.YELLOW + "Reset instruction block on " + page);

                } else {
                    XBotDebug.warn("AIV", "User-reported header not found on " + page);
                    if (!content.contains("<!-- HBC AIV helperbot WARNING -->")) {
                        if (!originalContent.equals(bot.getParent().getWiki().getPageText(page))) {
                            XBotDebug.warn("AIV", ChatColor.BLUE + page + ChatColor.YELLOW + " has changed since we read it, not changing.");
                            return;
                        } else {
                            new Edit(page, content + "<!-- HBC AIV helperbot WARNING -->\n", "WARNING: User-reported header not found!", time).run(bot.getParent(), bot.getRunPage());
                        }
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
