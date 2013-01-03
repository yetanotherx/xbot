package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.bots.BotThread;
import static com.yetanotherx.xbot.util.RegexUtil.getMatcher;
import static com.yetanotherx.xbot.util.Util.toLong;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * TODO: Runpage
 */
public class AIVBot extends BotThread {

    protected final int readRate = 30;
    protected final String[] pages = new String[]{
        "Wikipedia:Administrator intervention against vandalism",
        "Wikipedia:Administrator intervention against vandalism/TB2",
        "Wikipedia:Usernames for administrator attention",
        "Wikipedia:Usernames for administrator attention/Bot",
        //"Wikipedia:Usernames for administrator attention/Holding pen"
    };
    protected final String[] params = new String[]{
        "RemoveBlocked",
        "MergeDuplicates",
        "AutoMark",
        "FixInstructions",
        "AutoBacklog"
    };
    private final String version = "2.0.23";
    private String instructions = "";
    private Map<String, String> ips;
    private List<String> categories;

    public AIVBot(XBot main, String name) {
        super(main, name);

    }

    @Override
    public void doRun() {
        this.addJob(new GetIPListJob(this, toLong(0, 0, 5, 0, 0), true));
        this.addJob(new GetInstructionsJob(this, toLong(0, 0, 15, 0, 0), true));

        for (String page : pages) {
            if( !this.isRunning() ) {
                break;
            }
            this.addJob(new CheckPageJob(page, this, toLong(0, 0, 0, readRate, 0), true));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }
        }

        while (this.isRunning()) {
        }
    }

    @Override
    public void doShutdown() {
    }

    public synchronized String getInstructions() {
        return instructions;
    }

    public synchronized void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public synchronized List<String> getCategories() {
        return categories;
    }

    public synchronized void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public synchronized Map<String, String> getIPs() {
        return ips;
    }

    public synchronized void setIPs(Map<String, String> ips) {
        this.ips = ips;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Parses a line, depending on if the line is in a comment block
     * @param line
     * @param inComment
     * @return Array of {whether or not the block is still in a comment,
     *                  the line without any comments, the comments
     *                  that were removed}
     */
    protected static String[] parseComment(String line, boolean inComment) {
        boolean commentStart = false;
        boolean commentEnd = false;
        String remainder = "";

        if (inComment) {
            // Check if an opened comment ends in this line
            if (line.contains("-->")) {
                // We're in a comment block, but it's ending
                Matcher m = getMatcher("(.*?-->)", line);
                if (m.find()) {
                    remainder = m.group(1);
                    line = m.replaceAll("");
                    inComment = false;
                    commentEnd = true;
                }
            }
        }

        line = line.replaceAll("<!--.*?-->", "");

        Matcher m = getMatcher("<!--.*", line);
        if (m.find()) {
            line = m.replaceAll("");
            inComment = true;
            commentStart = true;
        }

        return new String[]{String.valueOf(inComment), line, remainder};
    }

    public static String getReportSummary(int reportCount) {
        String tally = "Empty.";
        if (reportCount != 0) {
            tally = reportCount + " report" + (reportCount == 1 ? "" : "s") + " remaining. ";
        }
        return tally;
    }
}
