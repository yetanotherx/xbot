package com.yetanotherx.xbot;

import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.util.RegexUtil;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.security.auth.login.FailedLoginException;

public class XBotWiki extends NewWiki {

    private static final long serialVersionUID = 7139487259719L;
    private XBot parent;
    private boolean enableWriteThrottling = false;

    public XBotWiki(XBot parent, String domain, String scriptPath) {
        super(domain, scriptPath);
        this.parent = parent;
        this.setParams();

    }

    public XBotWiki(XBot parent, String domain) {
        super(domain);
        this.parent = parent;
        this.setParams();
    }

    public XBotWiki(XBot parent) {
        this.parent = parent;
        this.setParams();
    }

    public void begin() {
        try {
            this.login(parent.getConf().getUsername(), parent.getConf().getPassword());
        } catch (FailedLoginException e) {
            XBotDebug.error("Wiki", "Login error: " + e.getMessage(), e);
            throw new RuntimeException("Received a login error.", e);
        } catch (IOException e) {
            XBotDebug.error("Wiki", "IP exception gotten. Is the wiki down?", e);
            throw new RuntimeException("Received a login error.", e);
        }
    }

    public boolean checkRunpage(String page) {
        try {
            String text = this.parent.getWiki().getPageText(page);
            if (!text.toLowerCase().trim().matches("(yes|run|enable|go)")) {
                XBotDebug.error("Wiki", "Bot disabled via " + page + "!");
                return false;
            }
        } catch (IOException ex) {
        }
        return true;
    }

    public void login(String user, String pass) throws IOException, FailedLoginException {
        XBotDebug.info("Wiki", ChatColor.PINK + "Logging in to " + this.getDomain() + " as " + user);
        super.login(user.trim(), pass.trim().toCharArray());
    }

    public void shutdown() {
    }

    private void setParams() {
        int mask = NewWiki.ASSERT_LOGGED_IN;
        if (this.parent.getConf().doCheckTalk()) {
            mask |= NewWiki.ASSERT_NO_MESSAGES;
        }
        if (this.parent.getConf().doConfirmBot()) {
            mask |= NewWiki.ASSERT_BOT;
        }
        this.setAssertionMode(mask);

        this.setMaxLag(this.parent.getConf().getMaxlag());
        this.setResolveRedirects(true);

        this.setThrottle(0);

        this.setUserAgent("XBot v" + XBot.getVersion() + " using MER-C's Wiki.java");
        this.setUsingCompressedRequests(true);
    }

    public synchronized void doEdit(String title, String text, String summary, boolean minor) {
        if (parent.getConf().doFollowNoBots() && this.failsNoBots(text)) {
            XBotDebug.warn("Wiki", "Could not write to " + title + ": Failed the {{nobots}} check.");
            return;
        }

        Edit e = new Edit(title, text, summary, minor);
        this.runEdit(e);
        if (this.enableWriteThrottling) {
            if (this.parent.getConf().getEditRate() > 0) {
                try {
                    Thread.sleep(60000 / this.parent.getConf().getEditRate());
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private void runEdit(Edit edit) {
        String title = edit.title;
        String text = edit.text;
        String summary = edit.summary;
        boolean minor = edit.minor;

        try {
            super.edit(title, text, summary, minor, isMarkBot(), -2, null);
        } catch (Exception ex) {
            XBotDebug.error("Wiki", "Error writing to " + title, ex);
        }
    }

    private boolean failsNoBots(String text) {
        return RegexUtil.matches("(?si).*\\{\\{(nobots|bots\\|(allow=none|deny=(.*?" + parent.getConf().getUsername() + ".*?|all)|optout=all))\\}\\}.*", text, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected String fetch(String url, String caller) throws IOException {
        parent.getMonitor().newAPICall();
        return super.fetch(url, caller);
    }

    @Override
    protected String multipartPost(String url, Map<String, ?> params, String caller) throws IOException {
        parent.getMonitor().newAPICall();
        return super.multipartPost(url, params, caller);
    }

    @Override
    protected String post(String url, String text, String caller) throws IOException {
        parent.getMonitor().newAPICall();
        return super.post(url, text, caller);
    }

    public boolean doEnableWriteThrottling() {
        return enableWriteThrottling;
    }

    public void setEnableWriteThrottling(boolean enableWriteThrottling) {
        this.enableWriteThrottling = enableWriteThrottling;
    }

    public static class Edit {

        public final String title, text, summary;
        public final boolean minor;

        public Edit(String title, String text, String summary, boolean minor) {
            this.title = title;
            this.text = text;
            this.summary = summary;
            this.minor = minor;
        }
    }
}
