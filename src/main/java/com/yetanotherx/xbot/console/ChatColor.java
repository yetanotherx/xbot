package com.yetanotherx.xbot.console;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum for color control characters
 * 
 * @author Spout
 */
public enum ChatColor {

    BLACK(0x0),
    DARK_BLUE(0x1),
    DARK_GREEN(0x2),
    DARK_CYAN(0x3),
    DARK_RED(0x4),
    PURPLE(0x5),
    GOLD(0x6),
    GRAY(0x7),
    DARK_GRAY(0x8),
    BLUE(0x9),
    BRIGHT_GREEN(0xA),
    CYAN(0xB),
    RED(0xC),
    PINK(0xD),
    YELLOW(0xE),
    WHITE(0xF);
    private static final ChatColor[] codeLookup = new ChatColor[ChatColor.values().length];
    private static final Map<String, ChatColor> nameLookup = new HashMap<String, ChatColor>();
    private static final Pattern matchPatern = Pattern.compile("\\u00A7([0-9a-fA-F])");

    static {
        for (ChatColor color : values()) {
            codeLookup[color.code] = color;
            nameLookup.put(color.name(), color);
        }
    }

    public static ChatColor byCode(int code) {
        if (code < 0) {
            return null;
        }

        if (code >= codeLookup.length) {
            return null;
        }

        return codeLookup[code];
    }

    public static ChatColor byName(String name) {
        if (name == null) {
            return null;
        }

        Matcher matcher = matchPatern.matcher(name);
        if (matcher.matches()) {
            int code;
            try {
                code = Integer.parseInt(matcher.group(1), 16);
                return byCode(code);
            } catch (NumberFormatException ignore) {
                // If it isn't a number, we can try by name lookup
            }
        }

        return nameLookup.get(name.toUpperCase());
    }
    private final int code;

    private ChatColor(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "\u00A7" + Integer.toString(code, 16);
    }

    public static String strip(String str) {
        return str.replaceAll("\\u00A7[0-9a-fA-F]", "");
    }

    public static boolean isColored(String str) {
        return str.contains("\u00A7");
    }
}
