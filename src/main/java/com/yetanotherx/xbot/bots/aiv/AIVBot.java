package com.yetanotherx.xbot.bots.aiv;

import com.yetanotherx.xbot.XBot;
import com.yetanotherx.xbot.bots.BotThread;
import static com.yetanotherx.xbot.util.Util.toLong;
import java.util.List;
import java.util.Map;

public class AIVBot extends BotThread {

    protected int readRate = 15;  //TODO: Make this configurable
    protected final String[] pages = new String[] {
        "Wikipedia:Administrator intervention against vandalism",
        "Wikipedia:Administrator intervention against vandalism/TB2",
        "Wikipedia:Usernames for administrator attention",
        "Wikipedia:Usernames for administrator attention/Bot",
        "Wikipedia:Usernames for administrator attention/holding pen"
    };
    
    private String instructions = "";
    private Map<String, String> ips;
    private List<String> categories;
    
    public AIVBot(XBot main, String name) {
        super(main, name);
        this.addJob(new GetIPListJob(this, toLong(0, 0, 5, 0, 0), true));
        this.addJob(new GetInstructionsJob(this, toLong(0, 0, 15, 0, 0), true));
        
        for( String page : pages ) {
            this.addJob(new CheckPageJob(page, this, toLong(0, 0, 0, readRate, 0), true));
        }
    }

    @Override
    public void doRun() {
        while(this.isRunning()) {
            
        }
    }

    @Override
    public void doShutdown() {
    }

    public synchronized String getInstructions() {
        return instructions;
    }

    public synchronized void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public synchronized List<String> getCategories() {
        return categories;
    }

    public synchronized void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public synchronized Map<String, String> getIPs() {
        return ips;
    }

    public synchronized void setIPs(Map<String, String> ips) {
        this.ips = ips;
    }
    
}
