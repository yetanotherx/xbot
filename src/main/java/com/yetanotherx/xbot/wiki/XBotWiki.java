package com.yetanotherx.xbot.wiki;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.console.ChatColor;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import javax.security.auth.login.FailedLoginException;

public class XBotWiki extends NewWiki {

    private static final long serialVersionUID = 7139487259719L;
    private XBot parent;

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
            XBotDebug.error("Wiki", "IO exception gotten. Is the wiki down?", e);
            throw new RuntimeException("Received a login error.", e);
        }
    }

    public void login(String user, String pass) throws IOException, FailedLoginException {
        XBotDebug.info("Wiki", ChatColor.PINK + "Logging in to " + this.getDomain() + " as " + user);
        super.login(user.trim(), pass.trim().toCharArray());
    }

    public void shutdown() {
    }

    public Calendar getTimestamp() {
        Calendar c = this.makeCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        return c;
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

}
