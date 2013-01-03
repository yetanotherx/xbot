package com.yetanotherx.xbot.bots.aiv;

import com.google.common.net.InetAddresses;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.console.ChatColor;
import com.yetanotherx.xbot.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import static com.yetanotherx.xbot.util.RegexUtil.*;

public class GetIPListJob extends BotJob<AIVBot> {

    public GetIPListJob(AIVBot bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        try {
            XBotDebug.info("AIV", ChatColor.GRAY + "Getting IP list...");

            String content = bot.getParent().getWiki().getPageText("User:HBC AIV helperbot/Special IPs");
            if (!content.isEmpty()) {
                Map<String, String> ips = new HashMap<String, String>();
                List<String> cats = new ArrayList<String>();

                for (String line : content.split("\n")) {
                    line = line.trim();

                    Matcher m = getMatcher("^\\* \\[\\[:Category:(.*?)\\]\\]$", line);
                    if (m.find()) {
                        cats.add(m.group(1));
                        continue;
                    }

                    m = getMatcher("^;(.*?):(.*)$", line);
                    if (m.find()) {
                        String ip = m.group(1);
                        String note = "This IP matches the mask (" + ip + ") in my [[User:HBC AIV helperbot/Special IPs|special IP list]] which is marked as: " + m.group(2);
                        String ip_no_range = ip.replaceAll("/\\d{1,2}$", "");

                        if (!InetAddresses.isInetAddress(ip_no_range)) {
                            continue;
                        }

                        ips.put(ip, note);
                    }

                }

                bot.setIPs(ips);
                bot.setCategories(cats);

                XBotDebug.debug("AIV", ChatColor.GRAY + "IPs fetched, will recheck in " + ChatColor.BLUE + Util.millisToString(wait));
            }
        } catch (IOException ex) {
            XBotDebug.error("AIV", "Could not read from wiki.", ex);
        }
    }

    @Override
    public void doShutdown() {
    }
}
