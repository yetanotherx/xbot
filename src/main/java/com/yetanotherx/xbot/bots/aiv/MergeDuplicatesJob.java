package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.util.Util;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.yetanotherx.xbot.util.RegexUtil.*;

public class MergeDuplicatesJob extends BotJob<AIVBot> {

    private String page;

    public MergeDuplicatesJob(AIVBot bot, String page) {
        super(bot);
        this.page = page;
    }

    @Override
    public void doRun() {
        try {
            String content = bot.getParent().getWiki().getPageText(page);

            if (!content.isEmpty()) {
                List<String> newContent = new LinkedList<String>();
                Map<String, Integer> userTable = new HashMap<String, Integer>();
                int reportCount = 0;
                boolean inComment = false;

                List<String> contentList = Arrays.asList(content.split("\n"));
                while (contentList.size() > 0) {
                    String line = contentList.remove(0);

                    String[] comment = AIVBot.parseComment(line, inComment);
                    inComment = Boolean.parseBoolean(comment[0]);
                    String bareLine = comment[1];

                    if (line.equals("\n")) {
                        continue;
                    }

                    Matcher m = getMatcher("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(.*?)\\s*\\}\\}", bareLine, Pattern.CASE_INSENSITIVE);
                    if ((inComment && line.equals(bareLine)) || !m.matches()) {
                        // Either we're in a comment block, or the line doesn't match any template
                        newContent.add(line);
                        continue;
                    }

                    String user = m.group(2);
                    m = getMatcher("^((?:1|user)=)", user, Pattern.CASE_INSENSITIVE);
                    if (m.matches()) {
                        user = user.replace(m.group(1), "");
                    }

                    if (!user.isEmpty()) {
                        if (userTable.containsKey(user)) {
                            if (line.startsWith("*")) {
                                line = line.substring(1);
                            }

                            getMatcher("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(.*?)\\s*\\}\\}", line, Pattern.CASE_INSENSITIVE).replaceAll("");

                            String oldLine = newContent.get(userTable.get(user));
                            oldLine += "\n:*" + line + " <small><sup>(Moved by bot)</sup></small>";
                            newContent.set(userTable.get(user), oldLine);
                        } else {
                            newContent.add(line);
                            userTable.put(user, newContent.size() - 1);

                            while (contentList.size() > 0 && !lcMatches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|", contentList.get(0)) && !contentList.get(0).contains("<!--")) {
                                String myComment = contentList.remove(0);
                                inComment = Boolean.parseBoolean(AIVBot.parseComment(myComment, inComment)[0]);

                                String oldLine = newContent.get(userTable.get(user));
                                oldLine += "\n" + comment;
                                newContent.set(userTable.get(user), oldLine);
                            }
                            reportCount++;
                        }
                    }
                }

                String tally = AIVBot.getReportSummary(reportCount);
                String summary = "Duplicate entries merged";

                String newCont = Util.join("\n", newContent);
                if (!newCont.isEmpty()) {
                    this.bot.getParent().getWiki().doEdit(page, newCont, tally + summary, false);
                }

                XBotDebug.info("AIV", ChatColor.GOLD + " Duplicates merged on " + ChatColor.BLUE + page);
            }
        } catch (IOException ex) {
            XBotDebug.error("AIV", "Could not read from wiki.", ex);
        }
    }

    @Override
    public void doShutdown() {
    }
}
