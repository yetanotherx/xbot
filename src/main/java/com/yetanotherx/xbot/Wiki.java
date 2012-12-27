package com.yetanotherx.xbot;

import net.sourceforge.jwbf.core.actions.ContentProcessable;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.bots.util.JwbfException;
import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 * TODO: NoBots
 * TODO: Runpage
 */
public class Wiki extends MediaWikiBot {

    private XBot parent;

    public Wiki(XBot parent, HttpActionClient client) {
        super(client);
        this.parent = parent;
    }

    public void begin() {
        boolean keepTrying = false;
        while (keepTrying) {
            try {
                this.login(parent.getConf().getUsername(), parent.getConf().getPassword());
                keepTrying = false;
            } /* catch (ThrottledException e) { TODO
                XBotDebug.info(name, "Login throttled, waiting " + e.getWait() + " seconds");
                Thread.sleep(e.getWait() * 1000);
            } */ catch (JwbfException e) {
                XBotDebug.error("Wiki", "Login error: " + e.getMessage(), e);
                keepTrying = false;
            }
        }
    }
    
    public void login(String user, String pass) {
        this.login(user, pass);
        XBotDebug.info("Wiki", "Logged in to " + this.getHostUrl() + " as " + user);
    }
    
    public void logout() {
        this.logout();
        XBotDebug.info("Wiki", "Logged out of " + this.getHostUrl());
    }

    @Override
    public synchronized String performAction(ContentProcessable a) {
        return super.performAction(a);
    }

    @Override
    public synchronized void writeContent(SimpleArticle simpleArticle) {
        super.writeContent(simpleArticle);
    }
    
    
    

    /*public List<SimpleArticle> getEmbeddedIn(SimpleArticle page, int count, ) {
        return null;
    }
    
    public List<SimpleArticle> getCategoryMembers() {
        return null;
    }
    
    public List<SimpleArticle> getImageUse() {
        return null;
    }
    
    public List<SimpleArticle> getLinksToExternal() {
        return null;
    }
    
    public Diff getDiff() {
        return null;
    }
    
    public List<SimpleArticle> getPrefixIndex() {
        return null;
    }*/
}
