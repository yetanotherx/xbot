package com.yetanotherx.xbot.bots;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.XBotDebug;
import com.yetanotherx.xbot.console.ChatColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Individual "bot" process.
 * 
 * @author yetanotherx
 */
public abstract class BotThread extends Thread {

    /**
     * Controller instance
     */
    protected XBot parent;
    /**
     * Name of the bot
     */
    private String name;
    /**
     * List of all jobs assigned to this thread
     */
    private final List<BotJob<? extends BotThread>> jobs = Collections.synchronizedList(new ArrayList<BotJob<? extends BotThread>>());
    /**
     * Whether or not the bot is enabled. It is enabled whenever the thread is
     * active.
     */
    private boolean enabled = false;
    /**
     * Whether or not the bot is running. It is enabled whenever the main loop
     * is active. (this is not set during shutdown, while enabled is)
     */
    private boolean running = false;
    
    public BotThread(XBot main, String name) {
        this.parent = main;
        this.setName(getName() + "-" + name);
        this.name = name;

        XBotDebug.info("MAIN", ChatColor.BLUE + "Initializing bot " + name);
    }

    public void run() {
        this.enabled = true;
        this.running = true;

        this.doRun();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }

        // doRun is over, we'll shut down when all the jobs die.
        while (this.running) {
            boolean isAlive = false;
            for (BotJob<? extends BotThread> job : this.getJobs()) {
                if (job.isEnabled()) {
                    isAlive = true; // Checks if any job is still running
                }
            }
            if (!isAlive) { // All jobs are disabled
                break;
            }

            try {
                Thread.sleep(100); // Don't overwhelm the processor
            } catch (InterruptedException ex) {
            }
        }

        this.shutdown();
    }

    /**
     * Shuts down the bot.
     */
    private synchronized void shutdown() {
        XBotDebug.debug(getRealName(), ChatColor.DARK_RED + "Shutting down bot thread " + getRealName());

        this.running = false;

        for (BotJob<? extends BotThread> job : this.getJobs()) {
            job.disable();
        }

        this.doShutdown();
        this.enabled = false;
    }

    public synchronized void disable() {
        this.running = false;
    }

    public synchronized boolean isEnabled() {
        return this.enabled;
    }

    public synchronized boolean isRunning() {
        return this.running;
    }

    public abstract void doRun();

    public abstract void doShutdown();
    
    public abstract String getRunPage();

    /**
     * Adds a job to the thread, and starts it.
     * @param job 
     */
    public synchronized void addJob(BotJob<? extends BotThread> job) {
        //XBotDebug.info("Adding job " + job.getClass().getCanonicalName());
        jobs.add(job);
        job.start();
    }

    public synchronized List<BotJob<? extends BotThread>> getJobs() {
        return jobs;
    }

    public XBot getParent() {
        return parent;
    }

    public String getRealName() {
        return name;
    }

    public String toString() {
        synchronized (this) {
            return "BotThread[name=" + name + ", jobs=" + jobs.size() + ", enabled=" + enabled + ", running=" + running + "]";
        }
    }
}
