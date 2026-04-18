package com.guessgame.client.command;

public interface Command {
    void setArgs(String[] args);
    void execute();
    String dump();
}
