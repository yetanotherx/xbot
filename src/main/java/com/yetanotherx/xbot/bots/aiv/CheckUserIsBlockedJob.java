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

public class CheckUserIsBlockedJob extends BotJob<AIVBot> {

    private String page;
    private String user;

    public CheckUserIsBlockedJob(AIVBot bot, String user, String page) {
        super(bot);
        this.user = user;
        this.page = page;
    }

    @Override
    public void doRun() {
        try {
            String content = bot.getParent().getWiki().getPageText(page);

            if (!content.isEmpty()) {
                
            }
        } catch (IOException ex) {
            XBotDebug.error("AIV", "Could not read from wiki.", ex);
        }
    }

    @Override
    public void doShutdown() {
    }
}
