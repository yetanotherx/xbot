package com.yetanotherx.xbot.bots;

import com.yetanotherx.xbot.bots.aiv.AIVBot;
import com.yetanotherx.xbot.bots.example.ExampleBot;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains all the bots in the XBot package
 * 
 * @author yetanotherx
 */
public class BotRegistration {
    
    public final static Map<String, Class<? extends BotThread>> botList;
    
    static {
        Map<String, Class<? extends BotThread>> list = new HashMap<String, Class<? extends BotThread>>();
        //list.put("AIV", AIVBot.class);
        list.put("Example", ExampleBot.class);
        
        botList = Collections.unmodifiableMap(list);
    }
    
}
