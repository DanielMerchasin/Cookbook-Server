package com.daniel.myrecipes.utils;

public class Logger {

    public enum PrintLevel { DEBUG, INFO, WARN, ERROR, FATAL, NONE }

    private static PrintLevel printLevel = PrintLevel.INFO;

    public static PrintLevel getPrintLevel() {
        return printLevel;
    }

    public static void setPrintLevel(PrintLevel printLevel) {
        Logger.printLevel = printLevel;
    }

    public static void setPrintLevel(int printLevel) {
        Logger.setPrintLevel(printLevel < 0 || printLevel >= PrintLevel.values().length
                ? PrintLevel.DEBUG
                : PrintLevel.values()[printLevel]);
    }

    private static void print(PrintLevel v, String format, Object... args) {
        if (v.ordinal() < printLevel.ordinal())
            return;

        String message = TimeFormatter.formatDate() + v.name() + ": "
                + String.format(format, args);

        if (v.ordinal() >= PrintLevel.WARN.ordinal()) {
            System.err.println(message);
        } else {
            System.out.println(message);
        }
    }

    public static void debug(String format, Object... args) {
        print(PrintLevel.DEBUG, format, args);
    }

    public static void info(String format, Object... args) {
        print(PrintLevel.INFO, format, args);
    }

    public static void warn(String format, Object... args) {
        print(PrintLevel.WARN, format, args);
    }

    public static void error(String format, Object... args) {
        print(PrintLevel.ERROR, format, args);
    }

    public static void fatal(String format, Object... args) {
        print(PrintLevel.FATAL, format, args);
    }

}
