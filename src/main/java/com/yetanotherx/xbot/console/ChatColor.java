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

    BLACK(0x0, "0;0m", 1),
    DARK_BLUE(0x1, "0;34m", 2),
    DARK_GREEN(0x2, "0;32m", 3),
    DARK_CYAN(0x3, "0;36m", 10),
    DARK_RED(0x4, "0;31m", 5), // IRC = brown
    PURPLE(0x5, "0;35m", 6),
    GOLD(0x6, "0;33m", 7), // IRC = orange
    GRAY(0x7, "0;37m", 15),
    DARK_GRAY(0x8, "1;30m", 14),
    BLUE(0x9, "1;34m", 12),
    BRIGHT_GREEN(0xA, "1;32m", 9),
    CYAN(0xB, "1;36m", 11),
    RED(0xC, "1;31m", 4),
    PINK(0xD, "1;35m", 13),
    YELLOW(0xE, "1;33m", 8),
    WHITE(0xF, "1;37m", 0),
    RESET(0x10, "0m", -1);
    private static final ChatColor[] codeLookup = new ChatColor[ChatColor.values().length];
    private static final Map<String, ChatColor> nameLookup = new HashMap<String, ChatColor>();
    private static final Pattern matchPatern = Pattern.compile("\\u00A7([X0-9a-fA-F])");

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
    private final String ansi;
    private final int irc;

    private ChatColor(int code, String ansi, int irc) {
        this.code = code;
        this.ansi = ansi;
        this.irc = irc;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        if( code > 0xF ) {
            return "\u00A7X";
        }
        return "\u00A7" + Integer.toString(code, 16);
    }

    public String toIRCColor() {
        if (irc < 0) {
            return (char) 3 + "";
        }
        return ((char) 3) + irc + "";
    }

    public String toANSIColor() {
        return "\033[" + ansi;
    }

    public static String getIRCString(String str) {
        if (isColored(str)) {
            for (ChatColor c : values()) {
                str = str.replaceAll(c.toString(), c.toIRCColor());
            }
        }
        return str;
    }

    public static String getANSIString(String str) {
        if (isColored(str)) {
            for (ChatColor c : values()) {
                str = str.replaceAll(c.toString(), c.toANSIColor());
            }
        }
        return str;
    }

    public static String strip(String str) {
        return str.replaceAll("\\u00A7[X0-9a-fA-F]", "");
    }

    public static boolean isColored(String str) {
        return str.contains("\u00A7");
    }
}
