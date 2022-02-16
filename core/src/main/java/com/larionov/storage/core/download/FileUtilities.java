package com.larionov.storage.core.download;

public class FileUtilities {
    private static final String[] prefixes = new String[] {"", "K", "M", "G"};
    private static final String byteSymbol = "B";
    private static final long SECONDS_DIVIDER = 1000000000L;
    private static final long MINUTES_DIVIDER = 1000000000L * 60;
    private static final long HOUR_DIVIDER = 1000000000L * 60 * 60;

    public static String timesToString(long time){
        int hours = (int) (time / HOUR_DIVIDER);
        long tomeLost = time % HOUR_DIVIDER;
        int minutes = (int) (tomeLost / MINUTES_DIVIDER);
        tomeLost = tomeLost % MINUTES_DIVIDER;
        int seconds = (int) (tomeLost / SECONDS_DIVIDER);

        String result = "";
        result += hours > 0 ? hours + ":" : "";
        result += minutes + ":" + seconds;

        return result;
    }

    public static String bytesToString(long bytes) {
        int i = 0;
        int shift = i * 10;
        while (bytes >> shift > 1024 && i < 3){
            i++;
            shift = i * 10;
        }

        return  (bytes >> shift)
                + ((i > 0) ? ("." + ((bytes >> ((i - 1) * 10)) - ((bytes >> shift << 10))) / 100) : "")
                + prefixes[i] + byteSymbol;
    }
}
