package com.yetanotherx.xbot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    
    private static Map<String, Pattern> cache = new HashMap<String, Pattern>();
    
    public static Matcher getMatcher(String pattern, String line, int flags) {
        if( !cache.containsKey(pattern) ) {
            cache.put(pattern, Pattern.compile(line, flags));
        }
        return cache.get(pattern).matcher(line);
    }
    
    public static Matcher getMatcher(String pattern, String line) {
        return getMatcher(pattern, line, 0);
    }
    
    public static boolean lcMatches(String needle, String haystack) {
        return haystack.toLowerCase().matches(needle.toLowerCase());
    }
    
}
