package com.guessgame.client;

class ExceptionManager {
    public static void handle(Exception e) {
        Logger.getInstance().exception(e);
    }
}
