package com.yetanotherx.xbot.threads;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;
import com.yetanotherx.xbot.console.ChatColor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MonitorThread extends Thread {

    private XBot bot;
    private long startTime = System.currentTimeMillis();
    private long apiCalls = 0;
    private boolean enabled = false;

    public MonitorThread(XBot bot) {
        XBotDebug.debug("MAIN", "Creating monitor thread.");
        this.bot = bot;
    }

    public void run() {
        this.enabled = true;

        int updateCount = 0;
        while (this.isEnabled()) {
            try {
                if( updateCount > 500 ) {
                    XBotDebug.info("Monitor", ChatColor.CYAN + "Cleaning out old jobs...");
                    updateCount = 0;
                    
                    int jobsRemoved = 0;
                    int botsRemoved = 0;
                    
                    Iterator<BotThread> biter = bot.getBots().iterator();
                    while(biter.hasNext()) {
                        BotThread t = biter.next();
                        
                        Iterator<BotJob<? extends BotThread>> jiter = t.getJobs().iterator();
                        while (jiter.hasNext()) {
                            BotJob<? extends BotThread> j = jiter.next();
                            if( !j.isEnabled() ) {
                                jiter.remove();
                                jobsRemoved++;
                            }
                        }
                        
                        if( !t.isEnabled() ) {
                            biter.remove();
                            botsRemoved++;
                        }
                    }
                    
                    XBotDebug.info("Monitor", ChatColor.CYAN + "Done cleaning jobs. " + ChatColor.YELLOW + jobsRemoved + ChatColor.CYAN + " jobs removed, " + ChatColor.YELLOW + botsRemoved + ChatColor.CYAN + " bots removed.");
                }
                
                updateCount++;
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    public boolean isEnabled() {
        return this.enabled && !this.isInterrupted();
    }

    public void disable() {
        this.enabled = false;
    }

    public synchronized int getBotCount() {
        int count = 0;
        for (BotThread ibot : bot.getBots()) {
            if (ibot.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    public int getThreadCount() {
        return Thread.getAllStackTraces().size();
    }

    public Thread[] getThreads() {
        return Thread.getAllStackTraces().keySet().toArray(new Thread[0]);
    }

    public synchronized int getJobCount() {
        int count = 0;
        for (BotThread ibot : bot.getBots()) {
            for (BotJob<? extends BotThread> ijob : ibot.getJobs()) {
                if (ijob.isEnabled()) {
                    count++;
                }
            }
        }
        return count;
    }

    public long[] getMemory() {
        long[] out = new long[4];
        out[0] = (Runtime.getRuntime().totalMemory() / 1024) / 1024;
        out[1] = (Runtime.getRuntime().freeMemory() / 1024) / 1024;
        out[2] = (Runtime.getRuntime().maxMemory() / 1024) / 1024;
        out[3] = out[0] - out[1];
        return out;
    }

    public long getMillisecondsRunning() {
        return System.currentTimeMillis() - startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public Map<String, String> getSystemInfo() {
        Map<String, String> out = new HashMap<String, String>();
        out.put("Number of free cores", String.valueOf(Runtime.getRuntime().availableProcessors()));
        out.put("Operating System", System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        out.put("Java information", System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        out.put("JVM information", System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        return out;
    }

    public synchronized void newAPICall() {
        apiCalls++;
    }

    public synchronized int getApiCallsPerMinute() {
        long minutes = ((getMillisecondsRunning() / 1000L) / 60L);

        if (minutes == 0) {
            return (int) apiCalls; // prevent apiCalls/0
        }

        return (int) (apiCalls / minutes);
    }
}
