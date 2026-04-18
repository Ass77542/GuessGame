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
            StringBuilder sb = new StringBuilder("Available rooms: {");
            for (String room : rooms) {
                sb.append(" ").append(room);
            }
            sb.append(" }");
            Logger.getInstance().info(sb.toString());
        }
    }



    public static class RoomJoined implements Response {
        private String roomName;
        private int maxPlayers;
        private int maxRounds;
        public RoomJoined(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {
            try {
                roomName = args[0];
                maxPlayers = Integer.parseInt(args[1]);
                maxRounds = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for max_players or max_rounds");
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for CreateRoom command");
            }
        }
        @Override
        public void execute() {
            RoomManager rm = RoomManager.getInstance();
            rm.joinRoom(roomName, maxPlayers, maxRounds);
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
         private String roomName;
         private String players;

        public GameStarted(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {
            try {
                roomName = args[0];
                players = args[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Bad server response");
            }
        }
        @Override
        public void execute() {
            RoomManager rm = RoomManager.getInstance();
            rm.p2pManager.startAccepting();

            List<String> playerEntries = Arrays.asList(players.split(","));

            rm.gameController.startGame(roomName, playerEntries, rm.getMaxRounds());
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
