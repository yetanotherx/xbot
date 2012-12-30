package com.yetanotherx.xbot.threads;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.bots.BotJob;
import com.yetanotherx.xbot.bots.BotThread;
import java.util.HashMap;
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

        while (this.isEnabled()) {
            try {
                // Database calls / minute
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
