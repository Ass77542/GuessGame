package com.guessgame.client.command;

import com.guessgame.client.manager.RoomManager;
import com.guessgame.client.Logger;

import com.guessgame.client.p2p.*;

import java.util.Arrays;
import java.util.List;

public class ServerResponse {
    public static class Connected implements Response {
        public Connected() {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
    };



    public static class RoomCreated implements Response {
        public RoomCreated(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {
            Logger.getInstance().info("Room created successfully.");
        }
    };



    public static class RoomList implements Response {
        public RoomList(/* let empty */) {}

        public String[] rooms;

        @Override
        public void setArgs(String[] args) {
            rooms = args;
        }
        @Override
        public void execute() {
            StringBuilder sb = new StringBuilder("Available rooms:\n");
            RoomManager.getInstance().clearRoom();
            for (String room : rooms) {
                sb.append("- ").append(room).append("\n");
                RoomManager.getInstance().addRoom(room);
            }
            Logger.getInstance().info(sb.toString());
        }
    }



    public static class RoomJoined implements Response {
        public RoomJoined(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {
            Logger.getInstance().info("Joined room successfully.");
        }
    };



     public static class RoomLeft implements Response {
        public RoomLeft(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {
            Logger.getInstance().info("Left room successfully.");
        }
    };



     public static class PlayerKicked implements Response {
        public PlayerKicked(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
     }



     public static class GameStarted implements Response {
        private String players;

        public GameStarted(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {
            try {
                players = args[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Bad server response");
            }
        }
        @Override
        public void execute() {
            List<String> playerEntries = Arrays.asList(players.split(","));

            RoomManager.getInstance().p2pManager.connectToPlayers(playerEntries);
        }
     };



     public static class ServerGameStarted implements Response {
        public ServerGameStarted(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
     };
}
