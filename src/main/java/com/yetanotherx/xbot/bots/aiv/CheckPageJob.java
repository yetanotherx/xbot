package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.console.ChatColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import org.apache.commons.net.util.SubnetUtils;

public class CheckPageJob extends BotJob<AIVBot> {

    private String page;

    public CheckPageJob(String page, AIVBot bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
        this.page = page;
    }

    @Override
    public void doRun() {
        try {
            XBotDebug.info("AIV", ChatColor.GRAY + "Getting page " + ChatColor.BLUE + page + ChatColor.GRAY + "...");

            String content = bot.getParent().getWiki().getPageText(page);
            if (!content.isEmpty()) {
                Matcher m = Pattern.compile("\\{\\{((?:no)?adminbacklog)\\}\\}\\s*<\\!-- (?:HBC AIV helperbot )?v([\\d.]+) ((?:\\w+=\\S+\\s+)+)-->", Pattern.CASE_INSENSITIVE).matcher(content);

                if (!m.find()) {
                    XBotDebug.warn("AIV", "Could not find parameter string on " + ChatColor.BLUE + page);
                } else {
                    boolean backlog = m.group(1).equals("adminbacklog");
                    String version = m.group(2);
                    String parameter_str = m.group(3);

                    if (!this.checkVersion(version)) {
                        XBotDebug.warn("AIV", "Version on " + ChatColor.BLUE + page + ChatColor.YELLOW + " is out of date! Required version: " + ChatColor.PINK + version);
                        return;
                    }

                    Map<String, String> params = this.parseParams(parameter_str);

                    if (params.get("FixInstructions").equals("on")) {
                        if (!this.checkInstructions(content)) {
                            return;
                        }
                    }

                    int reportCount = 0;
                    boolean inComment = false;
                    Map<String, Integer> userCount = new HashMap<String, Integer>();
                    boolean merged = false;
                    List<List<String>> ipCommentsNeeded = new ArrayList<List<String>>();

                    for (String line : content.split("\n")) {
                        String[] comment = this.parseComment(line, inComment);
                        inComment = Boolean.parseBoolean(comment[0]);
                        String bareLine = comment[1];

                        if (bareLine.equals(line) && inComment) {
                            continue;
                        }

                        m = Pattern.compile("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(.+?)\\s*\\}\\}", Pattern.CASE_INSENSITIVE).matcher(bareLine);
                        if (!m.find()) {
                            continue;
                        }

                        String user = m.group(2);
                        if (user.split("=").length > 1) {
                            user = user.split("=")[1];
                        }

                        reportCount++;
                        if (userCount.containsKey(user)) {
                            userCount.put(user, userCount.get(user) + 1);
                        } else {
                            userCount.put(user, 1);
                        }

                        if (userCount.get(user) > 1 && !merged && params.get("MergeDuplicates").equals("on")) {
                            XBotDebug.debug("AIV", ChatColor.GRAY + "Calling merge for " + ChatColor.PURPLE + user + ChatColor.GRAY + " on " + ChatColor.BLUE + page);
                            this.bot.addJob(new MergeDuplicatesJob(page, bot, 0, false));
                            merged = true;
                        }

                        if (params.get("RemoveBlocked").equals("on")) {
                            this.bot.addJob(new CheckUserJob(user, bot, 0, false));
                        }

                        List<String> cats = this.checkUserInCats(user, bot.getCategories());
                        if (!cats.isEmpty()) {
                            String message = "User is in the ";
                            message += (cats.size() > 1 ? "categories" : "category") + ": ";

                            for (int i = 0; i < cats.size() - 1; i++) {
                                message += "[[:Category:" + cats.get(i) + "|" + cats.get(i) + "]]";
                            }
                            message += "[[:Category:" + cats.get(cats.size() - 1) + "|" + cats.get(cats.size() - 1) + "]].";

                            bot.getIPs().put(user, message);
                        }

                        if (!merged && params.get("AutoMark").equals("on") && !line.contains("<!-- Marked -->")) {
                            for (String mask : bot.getIPs().keySet()) {
                                if (mask.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(?:/\\d{1,2})?$")) {
                                    SubnetUtils subnet = new SubnetUtils(mask);
                                    subnet.setInclusiveHostCount(true);
                                    if (subnet.getInfo().isInRange(user)) {
                                        ipCommentsNeeded.add(Arrays.asList(new String[]{page, user, mask}));
                                        break;
                                    }
                                } else {
                                    if (mask.equals(user)) {
                                        ipCommentsNeeded.add(Arrays.asList(new String[]{page, user, mask}));
                                        break;
                                    }
                                }
                            }
                        }

                    }

                    for (List<String> ipParams : ipCommentsNeeded) {
                        this.bot.addJob(new CommentSpecialIPJob(ipParams, reportCount));
                    }

                    if (params.get("AutoBacklog").equals("on") && !merged) {
                        if ((reportCount >= new Integer(params.get("AddLimit")) && !backlog)
                                || (reportCount <= new Integer(params.get("RemoveLimit")) && backlog)) {
                            this.bot.addJob(new FixBacklogJob(page, reportCount, params));
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

    private Map<String, String> parseParams(String parameter_str) {
        Map<String, String> out = new HashMap<String, String>();
        String[] split = parameter_str.split("\\s+");

        for (String def : "RemoveBlocked MergeDuplicates AutoMark FixInstructions AutoBacklog".split(" ")) {
            out.put(def, "off");
        }

        for (String splat : split) {
            String[] item = splat.split("=");
            if (item.length == 2) {
                out.put(item[0], item[1].toLowerCase());
            }
        }

        if (out.get("AutoBacklog").equals("on")) {
            if (!out.containsKey("AddLimit")) {
                out.put("AddLimit", "0");
            }
            if (!out.containsKey("RemoveLimit")) {
                out.put("RemoveLimit", "0");
            }
        }

        if (new Integer(out.get("AddLimit")) <= new Integer(out.get("RemoveLimit"))) {
            out.put("AutoBacklog", "off");
        }

        return out;
    }

    private boolean checkVersion(String version) {
        String[] activeVersion = version.split(".");
        String[] myVersion = this.bot.getVersion().split(".");

        if (activeVersion.length != myVersion.length) {
            return false;
        }

        for (int i = 0; i < activeVersion.length; i++) {
            int checkPart = Integer.parseInt(activeVersion[i]);
            int myPart = Integer.parseInt(myVersion[i]);

            if (checkPart < myPart) {
                return false;
            }
        }

        return true;
    }

    private boolean checkInstructions(String content) {
        if (!content.contains(bot.getInstructions())) {
            bot.addJob(new FixInstructionsJob(page));
            return false;
        }
        return true;
    }

    private String[] parseComment(String line, boolean inComment) {
        boolean commentStart = false;
        boolean commentEnd = false;
        String remainder = "";

        if (inComment) {
            // Check if an opened comment ends in this line
            if (line.contains("-->")) {
                Matcher m = Pattern.compile("(.*?-->)").matcher(line);
                if (m.find()) {
                    line = m.replaceAll("");
                    inComment = false;
                    commentEnd = true;
                    remainder = m.group(1);
                }
            }
        }

        line.replaceAll("<!--.*?-->", "");

        Matcher m = Pattern.compile("<!--.*").matcher(line);
        if (m.find()) {
            line = m.replaceAll("");
            inComment = true;
            commentStart = true;
        }
        
        return new String[] { String.valueOf(inComment), line, remainder };
    }

    private List<String> checkUserInCats(String user, List<String> categories) {
        List<String> out = new ArrayList<String>();
        
        SimpleArticle user_page = bot.getParent().getWiki().readData("User talk:" + user);
        user_page.
    }
}
