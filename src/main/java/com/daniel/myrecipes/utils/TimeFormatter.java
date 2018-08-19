package com.daniel.myrecipes.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatter {

    public static String formatDate() {
        return formatDate(System.currentTimeMillis());
    }

    public static String formatDate(long time) {
        return "[" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(time)) + "] ";
    }

    public static String formatNanos(long nanos) {
        return formatMillis(nanos / 1000000L);
    }

    /**
     * Format duration using this pattern: HH:mm:ss.SSS
     * Example: 00:12:53.935
     * @param millis
     * @return
     */
    public static String formatMillis(long millis) {
        StringBuilder sb = new StringBuilder("[");
        String sign = "";

        // Add sign if needed
        if (millis < 0) {
            sign = "-";
            millis *= -1;
        }

        // Calculate and format
        apd(sb, sign, 0, (millis / 3600000));
        millis %= 3600000;
        apd(sb, ":", 2, (millis / 60000));
        millis %= 60000;
        apd(sb, ":", 2, (millis / 1000));
        millis %= 1000;
        apd(sb, ".", 3, millis);

        // Close and return
        sb.append("]");

        return sb.toString();
    }

    private static void apd(StringBuilder sb, String prefix, int digit, long val) {
        sb.append(prefix);

        if (digit > 1) {
            int pad = digit - 1;
            for (long xa = val; xa > 9 && pad > 0; xa /= 10)
                pad--;
            for (int xa = 0; xa < pad; xa++)
                sb.append('0');
        }
        sb.append(val);
    }

}
