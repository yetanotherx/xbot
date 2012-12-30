package com.yetanotherx.xbot.bots;

/**
 * Individual jobs run by a bot thread
 * 
 * @author yetanotherx
 */
public abstract class BotJob<T extends BotThread> extends Thread {

    /**
     * Bot that owns this job
     */
    protected final T bot;
    
    /**
     * How long the thread should sleep after the job is complete
     */
    protected final long wait;
    
    /**
     * Whether or not to repeat the job indefinitely
     */
    protected final boolean repeat;
    
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

    public BotJob(T bot, long wait, boolean repeat) {
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
            
            while (running) {
                this.doRun();
                
                if( !repeat ) {
                    break;
                }

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
    
    public String toString() {
        return "BotJob[class=" + 
                getClass().getCanonicalName() + 
                ", thread=" + 
                bot.getRealName() + 
                ", wait=" + wait +
                ", repeat=" + repeat + 
                ", running=" + running +
                ", enabled=" + enabled + "]";
    }
}
