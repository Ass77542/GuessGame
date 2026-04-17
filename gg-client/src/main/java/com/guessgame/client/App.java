package com.guessgame.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.guessgame.client.manager.NetworkManager;
import com.guessgame.client.manager.RoomManager;

import com.guessgame.client.socket.TCPClient;
import com.guessgame.client.command.Command;
import com.guessgame.client.command.ServerCommand;
import com.guessgame.client.command.ServerResponse;
import com.guessgame.client.command.CommandFactory;

import com.guessgame.client.p2p.P2PManager;
import com.guessgame.client.logic.GameLogic;

public class App
{
    public static StringBuilder inputBuilder = new StringBuilder();

    public static String readStdin() {
        try {
            if (System.in.available() > 0) {
                int c = System.in.read();
                if (c == '\n') {
                    String input = inputBuilder.toString();
                    inputBuilder = new StringBuilder();
                    return input;
                } else {
                    inputBuilder.append((char) c);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void parseInput(String input) {
        String[] parts = input.split("\\|");
        if (parts.length < 2 || !parts[0].equals("GG")) {
            throw new IllegalArgumentException("Invalid command format");
        }
        String[] args = new String[parts.length - 2];
        for (int i = 2; i < parts.length; i++) {
            args[i - 2] = parts[i];
        }
        Command command = CommandFactory.getInstance().createCommand(parts[1], args);
        command.execute();
        NetworkManager.getInstance().sendCommand(command.dump());
    }

    public static void parseResponse(String response) {
        String[] parts = response.split("\\|");
        if (parts.length < 2 || !parts[0].equals("GG")) {
            throw new IllegalArgumentException("Invalid command format");
        }
        String[] args = new String[parts.length - 2];
        for (int i = 2; i < parts.length; i++) {
            args[i - 2] = parts[i];
        }
        Command command = CommandFactory.getInstance().createCommand(parts[1], args);
        command.execute();
    }

    public static void main(String[] args)
    {
        while (true) {
            String line;
            try {
                line = readStdin();
                if (line != null) {
                    parseInput(line);
                }
                NetworkManager.getInstance().flush();
                String response = NetworkManager.getInstance().receiveResponse();
                if (response != null) {
                    Logger.getInstance().debug("Received response: " + response);
                }
            } catch (Exception e) {
                ExceptionManager.handle(e);
            }
        }
    }
}
