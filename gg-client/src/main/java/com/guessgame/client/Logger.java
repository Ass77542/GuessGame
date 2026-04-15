package com.guessgame.client;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

public class Logger {
    private static Logger instance;
    private FileWriter fileWriter;

    private Logger() {
        try {
            fileWriter = new FileWriter("client.log", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    private void log(LogLevel level, String color, String message) {
        String formatted =
            "[" + color + level + "\u001B[0m" + "] " + getCurrentTime() + ": " + message;

        if (level == LogLevel.ERROR) {
            System.err.println(formatted);
        } else {
            System.out.println(formatted);
        }

        try {
            fileWriter.write(formatted + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, "\u001B[34m", message);
    }

    public void info(String message) {
        log(LogLevel.INFO, "\u001B[32m", message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, "\u001B[33m", message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, "\u001B[31m", message);
    }

    public void exception(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append(System.lineSeparator());
        }
        error(e.getClass() + ": " + e.getMessage() + System.lineSeparator() + sb.toString());
    }
}
