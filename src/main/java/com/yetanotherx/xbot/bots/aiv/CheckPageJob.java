package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.wiki.NewWiki.LogEntry;
import com.yetanotherx.xbot.wiki.NewWiki.User;
import org.joda.time.Period;
import com.yetanotherx.xbot.util.Util;
import com.yetanotherx.xbot.wiki.Edit;
import java.util.Calendar;
import java.util.LinkedList;
import com.google.common.net.InetAddresses;
import java.util.regex.Pattern;
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
import org.apache.commons.net.util.SubnetUtils;
import static com.yetanotherx.xbot.util.RegexUtil.*;

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
                Matcher m = getMatcher("\\{\\{((?:no)?adminbacklog)\\}\\}\\s*<!-- (?:HBC AIV helperbot )?v(\\S+?) ((?:\\w+=\\S+\\s+)+)-->", content, Pattern.CASE_INSENSITIVE);

                if (!m.find()) {
                    // No parameters...
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
                            return; // Instructions are messed up, don't do anything
                        }
                    }

                    int reportCount = 0;
                    boolean inComment = false;
                    Map<String, Integer> userCount = new HashMap<String, Integer>();
                    boolean merged = false;
                    List<List<String>> ipCommentsNeeded = new ArrayList<List<String>>();

                    for (String line : content.split("\n")) {
                        String[] comment = AIVBot.parseComment(line, inComment);
                        inComment = Boolean.parseBoolean(comment[0]);
                        String bareLine = comment[1];

                        if (bareLine.equals(line) && inComment) {
                            continue;   // We're in a comment block, not going to do anything
                        }

                        m = getMatcher("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(.+?)\\s*\\}\\}", bareLine, Pattern.CASE_INSENSITIVE);
                        if (!m.find()) {
                            continue; // Go to next line if it's not a vandal template
                        }

                        String user = m.group(2);
                        if (user.split("=").length > 1) {
                            user = user.split("=")[1];  // Get the username from the parameter name
                        }

                        reportCount++;
                        if (userCount.containsKey(user)) {
                            userCount.put(user, userCount.get(user) + 1);
                        } else {
                            userCount.put(user, 1);
                        }

                        if (userCount.get(user) > 1 && !merged && params.get("MergeDuplicates").equals("on")) {
                            XBotDebug.debug("AIV", ChatColor.GRAY + "Calling merge for " + ChatColor.PURPLE + user + ChatColor.GRAY + " on " + ChatColor.BLUE + page);
                            this.mergeDuplicates();
                            merged = true;
                        }

                        if (params.get("RemoveBlocked").equals("on")) {
                            this.checkUser(user);
                        }

                        List<String> cats = this.checkUserInCats(user, bot.getCategories());
                        if (!cats.isEmpty()) {
                            String message = "User is in the ";
                            message += (cats.size() > 1 ? "categories" : "category") + ": ";

                            for (int i = 0; i < cats.size() - 1; i++) {
                                message += "[[:Category:" + cats.get(i) + "|" + cats.get(i) + "]], ";
                            }
                            message += "[[:Category:" + cats.get(cats.size() - 1) + "|" + cats.get(cats.size() - 1) + "]].";

                            bot.getIPs().put(user, message);
                        }

                        if (!merged && params.get("AutoMark").equals("on") && !line.contains("<!-- Marked -->")) {
                            for (String mask : bot.getIPs().keySet()) {
                                if (mask.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/\\d{1,2}") && InetAddresses.isInetAddress(user) && user.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                                    SubnetUtils subnet = new SubnetUtils(mask);
                                    subnet.setInclusiveHostCount(true);
                                    if (subnet.getInfo().isInRange(user)) { // TODO: IPv6
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
                        this.commentSpecialIP(ipParams, reportCount);
                    }

                    if (params.get("AutoBacklog").equals("on") && !merged) {
                        if ((reportCount >= new Integer(params.get("AddLimit")) && !backlog)
                                || (reportCount <= new Integer(params.get("RemoveLimit")) && backlog)) {
                            this.setBacklog(reportCount, params);
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

        for (String def : AIVBot.params) {
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
            this.fixInstructions();
            return false;
        }
        return true;
    }

    private List<String> checkUserInCats(String user, List<String> categories) throws IOException {
        List<String> out = new ArrayList<String>();
        String[] pageCats = bot.getParent().getWiki().getCategories("User talk:" + user);
        for (String cat : pageCats) {
            if (categories.contains(cat.replace("Category:", ""))) {
                out.add(cat.replace("Category:", ""));
            }
        }
        return out;
    }

    private void mergeDuplicates() throws IOException {
        String content = bot.getParent().getWiki().getPageText(page);
        String originalContent = content.toString();
        Calendar time = bot.getParent().getWiki().getTimestamp();

        if (!content.isEmpty()) {
            List<String> newContent = new LinkedList<String>();
            Map<String, Integer> userTable = new HashMap<String, Integer>();
            int reportCount = 0;
            boolean inComment = false;

            List<String> contentList = new ArrayList<String>(Arrays.asList(content.split("\n")));
            while (contentList.size() > 0) {
                String line = contentList.remove(0);

                String[] comment = AIVBot.parseComment(line, inComment);
                inComment = Boolean.parseBoolean(comment[0]);
                String bareLine = comment[1];

                if (line.equals("\n")) {
                    continue;
                }

                Matcher m = getMatcher("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(.*?)\\s*\\}\\}", bareLine, Pattern.CASE_INSENSITIVE);
                if ((inComment && line.equals(bareLine)) || !m.find()) {
                    // Either we're in a comment block, or the line doesn't match any template
                    newContent.add(line);
                    continue;
                }

                String user = m.group(2);
                m = getMatcher("^((?:1|user)=)", user, Pattern.CASE_INSENSITIVE);
                if (m.find()) {
                    user = user.replace(m.group(1), "");
                }

                if (!user.isEmpty()) {
                    if (userTable.containsKey(user)) {
                        if (line.startsWith("*")) {
                            line = line.substring(1);
                        }

                        line = getMatcher("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(.*?)\\s*\\}\\}", line, Pattern.CASE_INSENSITIVE).replaceAll("");

                        String oldLine = newContent.get(userTable.get(user));
                        oldLine += "\n:*" + line + " <small><sup>(Moved by bot)</sup></small>";
                        newContent.set(userTable.get(user), oldLine);
                    } else {
                        newContent.add(line);
                        userTable.put(user, newContent.size() - 1);

                        while (contentList.size() > 0 && !matches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|", contentList.get(0), Pattern.CASE_INSENSITIVE) && !contentList.get(0).contains("<!--")) {
                            String myComment = contentList.remove(0);
                            inComment = Boolean.parseBoolean(AIVBot.parseComment(myComment, inComment)[0]);

                            String oldLine = newContent.get(userTable.get(user));
                            oldLine += "\n" + myComment;
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
                if (!originalContent.equals(bot.getParent().getWiki().getPageText(page))) {
                    XBotDebug.warn("AIV", ChatColor.BLUE + page + ChatColor.YELLOW + " has changed since we read it, not changing.");
                    return;
                } else {
                    new Edit(page, newCont, tally + summary, time).run(bot.getParent(), bot.getRunPage());
                }
            }

            XBotDebug.info("AIV", ChatColor.GOLD + " Duplicates merged on " + ChatColor.BLUE + page);
        }
    }

    private void checkUser(String user) throws IOException {
        LogEntry[] logs = bot.getParent().getWiki().getIPBlockList(user);
        if (logs.length > 0) { // should never be >1
            // User is blocked
            LogEntry log = logs[0];
            if (!log.getType().equals("block")) {
                return; // WTF???
            }

            //BLOCK_LOG new Object[] { boolean anononly, boolean nocreate, boolean noautoblock, boolean noemail, boolean nousertalk, String duration }
            Object[] details = (Object[]) log.getDetails();
            boolean paramAO = (Boolean) details[0];
            boolean paramNC = (Boolean) details[1];
            boolean paramNAB = (Boolean) details[2];
            boolean paramNEM = (Boolean) details[3];
            boolean paramNUT = (Boolean) details[4];
            String expiry = (String) details[5];

            User blocker = log.getUser();
            String duration = "indef";

            if (!expiry.equals("infinity")) {
                //2013-01-02T19:39:27Z
                int[] expiryDate = Util.parseTZDate(expiry);
                if (expiryDate == null) {
                    return;
                }
                long timeStampMS = log.getTimestamp().getTimeInMillis();
                long expiryMS = Util.dateToLong(expiryDate);

                Period period = new Period(Util.roundLong(timeStampMS), Util.roundLong(expiryMS));
                duration = Util.periodFormatter.print(period) + " ";
            }

            List<String> flags = new ArrayList<String>();
            if (paramAO) {
                flags.add("AO");
            }
            if (paramNC) {
                flags.add("ACB");
            }
            if (paramNAB) {
                flags.add("ABD");
            }

            String blockType = "";
            if (!flags.isEmpty()) {
                blockType = "[[User:HBC AIV helperbot/Legend|(" + Util.join(" ", flags) + ")]]";
            }

            this.removeName(user, blocker, duration, blockType);
        }


        String content = bot.getParent().getWiki().getPageText(page);

        if (!content.isEmpty()) {
        }
    }

    private void commentSpecialIP(List<String> ipParams, int reportCount) throws IOException {
        String thisPage = ipParams.get(0);
        String user = ipParams.get(1);
        String mask = ipParams.get(2);

        String content = bot.getParent().getWiki().getPageText(thisPage);
        String originalContent = content.toString();
        Calendar time = bot.getParent().getWiki().getTimestamp();

        if (!content.isEmpty()) {
            List<String> newContent = new LinkedList<String>();
            boolean inComment = false;

            for (String line : content.split("\n")) {
                inComment = Boolean.parseBoolean(AIVBot.parseComment(line, inComment)[0]);

                if (line.contains(user) && matches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)", line, Pattern.CASE_INSENSITIVE)) {
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
                if (!originalContent.equals(bot.getParent().getWiki().getPageText(thisPage))) {
                    XBotDebug.warn("AIV", ChatColor.BLUE + thisPage + ChatColor.YELLOW + " has changed since we read it, not changing.");
                    return;
                } else {
                    new Edit(thisPage, newCont, tally + summary, time).run(bot.getParent(), bot.getRunPage());
                }
            }

            XBotDebug.info("AIV", ChatColor.GOLD + user + " matched " + mask + ", marked as: " + bot.getIPs().get(mask));
        }
    }

    private void setBacklog(int reportCount, Map<String, String> params) throws IOException {
        int addLimit = new Integer(params.get("AddLimit"));
        int delLimit = new Integer(params.get("RemoveLimit"));

        String summary = "";

        String content = bot.getParent().getWiki().getPageText(page);
        String originalContent = content.toString();
        Calendar time = bot.getParent().getWiki().getTimestamp();

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
                    new Edit(page, newCont, summary, time).run(bot.getParent(), bot.getRunPage());
                }
            }
        }
    }

    private void removeName(String user, User blocker, String duration, String blockType) throws IOException {
        String content = bot.getParent().getWiki().getPageText(page);
        String originalContent = content.toString();
        Calendar time = bot.getParent().getWiki().getTimestamp();

        if (!content.isEmpty()) {
            int ipsLeft = 0;
            int usersLeft = 0;
            boolean found = false;
            int linesSkipped = 0;
            List<String> newContent = new LinkedList<String>();
            boolean inComment = false;

            List<String> contentList = new ArrayList<String>(Arrays.asList(content.split("\n")));
            while (contentList.size() > 0) {
                String line = contentList.remove(0);

                String[] comment = AIVBot.parseComment(line, inComment);
                inComment = Boolean.parseBoolean(comment[0]);
                String bareLine = comment[1];
                String remainder = comment[2];

                if (inComment || !matches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(?:1=|user=)?\\Q" + user + "\\E\\s*\\}\\}", line, Pattern.CASE_INSENSITIVE)) {
                    newContent.add(line);
                    if (inComment && line.equals(bareLine)) {
                        continue;
                    }
                    if (bareLine.contains("{{IPvandal|")) {
                        ipsLeft++;
                    }
                    if (matches("\\{\\{(vandal|userlinks|user-uaa)\\|", bareLine, Pattern.CASE_INSENSITIVE)) {
                        usersLeft++;
                    }
                } else {
                    found = true;
                    if (!remainder.isEmpty()) {
                        newContent.add(remainder);
                    }

                    while (contentList.size() > 0
                            && !matches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|", contentList.get(0), Pattern.CASE_INSENSITIVE)
                            && !contentList.get(0).startsWith("<!--")
                            && !contentList.get(0).startsWith("=")) {

                        String removed = contentList.remove(0);
                        if (!removed.isEmpty()) {
                            linesSkipped++;
                            inComment = Boolean.parseBoolean(AIVBot.parseComment(removed, inComment)[0]);
                        }
                    }
                }
            }

            content = Util.join("\n", newContent);
            if (!found || content.isEmpty()) {
                return;
            }

            String length = " ";
            if (!duration.isEmpty()) {
                if (duration.equals("indef")) {
                    length = " indef ";
                } else {
                    length = " " + duration;
                }
            }

            String tally = "Empty.";
            if (ipsLeft != 0 || usersLeft != 0) {
                String ipNote = ipsLeft + " IP" + ((ipsLeft != 1) ? "s" : "");
                String userNote = usersLeft + " user" + ((usersLeft != 1) ? "s" : "");

                if (usersLeft == 0) { // Only IPs left
                    tally = ipNote + " left.";
                } else if (ipsLeft == 0) { // Only users left
                    tally = userNote + " left.";
                } else { // Users and ips left
                    tally = ipNote + " & " + userNote + " left.";
                }
            }

            String skipped = "";
            if (linesSkipped > 0) {
                skipped = " " + linesSkipped + " comment(s) removed.";
            }

            String summary = tally + " rm [[Special:Contributions/" + user + "|" + user + "]] (blocked" + length + "by [[User:" + blocker.getUsername() + "|" + blocker.getUsername() + "]] " + blockType + "). " + skipped;
            if (!originalContent.equals(bot.getParent().getWiki().getPageText(page))) {
                XBotDebug.warn("AIV", ChatColor.BLUE + page + ChatColor.YELLOW + " has changed since we read it, not changing.");
                return;
            } else {
                new Edit(page, content, summary, time).run(bot.getParent(), bot.getRunPage());
            }
            XBotDebug.info("AIV", ChatColor.GOLD + "Removed " + ChatColor.YELLOW + user + ChatColor.GOLD + " on " + ChatColor.BLUE + page);
        }
    }

    private void fixInstructions() {
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
}
