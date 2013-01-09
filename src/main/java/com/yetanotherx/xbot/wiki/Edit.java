package com.yetanotherx.xbot.wiki;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.util.RegexUtil;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Pattern;

public class Edit {

    public final static int NO_FLAGS = 0;
    public final static int MINOR = 1;
    public final static int BOT = 2;
    public final static int NO_THROTTLE = 4;
    private String page;
    private String text;
    private String summary;
    private Calendar timestamp;
    private int flags = BOT;

    public Edit(String page, String text, String summary, Calendar timestamp, int flags) {
        this.page = page;
        this.text = text;
        this.summary = summary;
        this.timestamp = timestamp;
        this.flags = flags;
    }

    public Edit(String page, String text, String summary, Calendar timestamp) {
        this.page = page;
        this.text = text;
        this.summary = summary;
        this.timestamp = timestamp;
    }

    public int getFlags() {
        return flags;
    }

    public String getPage() {
        return page;
    }

    public String getSummary() {
        return summary;
    }

    public String getText() {
        return text;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMinor() {
        return (this.flags & MINOR) == MINOR;
    }

    public boolean isBot() {
        return (this.flags & BOT) == BOT;
    }

    public boolean isThrottled() {
        return (this.flags & NO_THROTTLE) != NO_THROTTLE;
    }

    private boolean failsNoBots(XBot parent, String text) {
        return RegexUtil.matches("(?si).*\\{\\{(nobots|bots\\|(allow=none|deny=(.*?" + parent.getConf().getUsername() + ".*?|all)|optout=all))\\}\\}.*", text, Pattern.CASE_INSENSITIVE);
    }

    public void run(XBot parent, String runpage) {
        if (parent.getConf().doFollowNoBots() && this.failsNoBots(parent, text)) {
            XBotDebug.warn("Wiki", "Could not write to " + page + ": Failed the {{nobots}} check.");
            return;
        }
        
        if( runpage != null && !this.checkRunpage(parent, runpage) ) {
            return;
        }

        try {
            parent.getWiki().edit(page, text, summary, isMinor(), isBot(), -2, timestamp);
        } catch (Exception ex) {
            XBotDebug.warn("Wiki", "Could not write to " + page, ex);
        }

        if (this.isThrottled() && parent.getConf().getEditRate() > 0) {
            try {
                Thread.sleep(60000 / parent.getConf().getEditRate());
            } catch (InterruptedException ex) {
            }
        }
    }
    
    private boolean checkRunpage(XBot parent, String page) {
        try {
            String runtext = parent.getWiki().getPageText(page);
            if (!runtext.toLowerCase().trim().matches("(yes|run|enable|go)")) {
                XBotDebug.error("Wiki", "Bot disabled via " + page + "!");
                return false;
            }
        } catch (IOException ex) {
        }
        return true;
    }
}
