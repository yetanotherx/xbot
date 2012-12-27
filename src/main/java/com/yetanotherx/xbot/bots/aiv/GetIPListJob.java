package com.yetanotherx.xbot.bots.aiv;

import com.google.common.net.InetAddresses;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.bots.BotJob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetIPListJob extends BotJob {

    public GetIPListJob(BotThread bot, long wait, boolean repeat) {
        super(bot, wait, repeat);
    }

    @Override
    public void doRun() {
        XBotDebug.debug("AIV", "Getting IP list...");
        AIVBot aivbot = (AIVBot) bot;
        
        String content = aivbot.getParent().getWiki().readData("User:HBC AIV helperbot/Special IPs").getText();
        if( !content.isEmpty() ) {
            Map<String, String> ips = new HashMap<String, String>();
            List<String> cats = new ArrayList<String>();
            
            for( String line : content.split("\n") ) {
                line = line.trim();
                
                Matcher m = Pattern.compile("^\\* \\[\\[:Category:(.*?)\\]\\]$").matcher(line);
                if( m.find() ) {
                    cats.add(m.group(1));
                    continue;
                }
                
                m = Pattern.compile("^;(.*?):(.*)$").matcher(line);
                if( m.find() ) {
                    String ip = m.group(1);
                    String note = "This IP matches the mask (" + ip + ") in my [[User:HBC AIV helperbot/Special IPs|special IP list]] which is marked as: " + m.group(2);
                    String ip_no_range = ip.replaceAll("/\\d{1,2}$", "");
                    
                    if( !InetAddresses.isInetAddress(ip_no_range) ) {
                        continue;
                    }
                    
                    ips.put(ip, note);
                }
                
            }
            
            aivbot.setIPs(ips);
            aivbot.setCategories(cats);
            
            XBotDebug.debug("AIV", "IPs fetched, will recheck later.");
        }
        
        
    }

    @Override
    public void doShutdown() {
    }
    
}
