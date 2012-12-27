package com.yetanotherx.xbot.bots;

/**
 * Individual jobs run by a bot thread
 * 
 * @author yetanotherx
 */
public abstract class BotJob extends Thread {

    /**
     * Bot that owns this job
     */
    protected final BotThread bot;
    
    /**
     * How long the thread should sleep after the job is complete
     */
    private final long wait;
    
    /**
     * Whether or not to repeat the job indefinitely
     */
    private final boolean repeat;
    
    /**
     * Whether or not the job is enabled.
     * Bot can be enabled, but not running.
     */
    private boolean enabled = false;
    
    /**
     * Whether or not the job is running.
     * Running is false when the main loop is exiting, but
     * the bot can be enabled even when the running is false (shutdown).
     */
    private boolean running = false;

    public BotJob(BotThread bot, long wait, boolean repeat) {
        this.bot = bot;
        this.wait = wait;
        this.repeat = repeat;
    }

    public void run() {
        this.running = true;
        this.enabled = true;
        
        try {
            while (!bot.isEnabled()) {
                //Wait until bot starts running
                Thread.sleep(50);
            }
            
            while (repeat && running) {
                this.doRun();

                Thread.sleep(wait + 50);
            }
            
        } catch (InterruptedException ex) {
        }
        
        this.shutdown();
    }

    public abstract void doRun();

    public abstract void doShutdown();

    /**
     * Stops the main loop, and calls the doShutdown() method.
     */
    private synchronized void shutdown() {
        this.running = false;
        
        this.doShutdown();
        
        this.enabled = false;
    }

    /**
     * Disables the job, which halts the main loop and runs the shutdown hooks
     */
    public void disable() {
        this.running = false;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isRunning() {
        return this.running;
    }
}
