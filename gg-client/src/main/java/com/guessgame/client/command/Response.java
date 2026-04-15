package com.guessgame.client.command;

public interface Response {
    void setArgs(String[] args);
    void execute();
}
