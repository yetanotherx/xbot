package com.yetanotherx.xbot;

import java.net.URL;
import net.sourceforge.jwbf.core.actions.ContentProcessable;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.ProcessException;

public class HttpActionClientThrottled extends HttpActionClient {

    private long lastAction = 0;
    private XBot parent;

    public HttpActionClientThrottled(XBot parent, URL url) {
        super(url);
        this.parent = parent;
    }

    @Override
    public synchronized String performAction(ContentProcessable contentProcessable) throws ActionException, ProcessException {
        return super.performAction(contentProcessable); //TODO: Throttle
    }
}
