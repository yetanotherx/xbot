package com.yetanotherx.xbot.util;

import java.util.Arrays;
import java.util.List;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class Util {

    /**
     * Converts the given number of days/hours/minutes/seconds/millis
     * to a single millisecond value. Used for assigning jobs.
     * @param days
     * @param hours
     * @param minutes
     * @param seconds
     * @param milliseconds
     * @return 
     */
    public static long toLong(int days, int hours, int minutes, int seconds, int milliseconds) {
        return milliseconds
                + (seconds * 1000)
                + (minutes * 60 * 1000)
                + (hours * 60 * 60 * 1000)
                + (days * 24 * 60 * 60 * 1000);
    }

    /**
     * Returns all parameters as a List<String>
     * @param params
     * @return 
     */
    public static List<String> asList(String... params) {
        return Arrays.asList(params);
    }

    /**
     * Converts milliseconds elapsed to a string representing elapsed time.
     * @param millis
     * @return 
     */
    public static String millisToString(long millis) {
        Period period = new Period(millis);

        PeriodFormatter formatter = new PeriodFormatterBuilder().appendWeeks().appendSuffix(" weeks, ").appendDays().appendSuffix(" days, ").appendHours().appendSuffix(" hours, ").appendMinutes().appendSuffix(" minutes, ").appendSeconds().appendSuffix(" seconds").printZeroNever().toFormatter();

        return formatter.print(period);

    }

    /**
     * Formats the number of millis since the epoch.
     * @param millis
     * @return 
     */
    public static String formatDate(long millis) {
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return format.print(millis);
    }
}
