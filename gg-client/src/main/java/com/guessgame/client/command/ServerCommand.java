package com.guessgame.client.command;

import com.guessgame.client.manager.NetworkManager;
import com.guessgame.client.manager.RoomManager;

import com.guessgame.client.p2p.P2PManager;
import com.guessgame.client.logic.GameController;

import java.util.Arrays;

class DummyUI implements GameController.GameUIListener {
    final String playerName;
    DummyUI(String name) { this.playerName = name; }

    @Override public void onFeedbackReceived(int c, int p) {
    System.out.println("[" + playerName + "-UI] Feedback reçu : couleurs=" + c + " positions=" + p);
    }
    @Override public void onWinner(String name) {
    System.out.println("[" + playerName + "-UI] Gagnant : " + name);
    }
    @Override public void onGameOver(String[] secret) {
    System.out.println("[" + playerName + "-UI] Game over. Secret=" + Arrays.toString(secret));
    }
    @Override public void onNewGame(boolean isHolder, String holder) {
    System.out.println("[" + playerName + "-UI] Nouvelle partie. Détenteur=" + holder);
    }
    @Override public void onSecretNeeded() {
    System.out.println("[" + playerName + "-UI] Vous devez choisir le secret !");
    }
    @Override public void onGuessNeeded(int remaining) {
    System.out.println("[" + playerName + "-UI] À vous de deviner ! Tentatives restantes=" + remaining);
    }
    @Override public void displayMessage(String msg) {
    System.out.println("[" + playerName + "-UI] " + msg);
    }
}

public class ServerCommand {
    public static class Connect implements Command {
        private String playerName;

        public Connect() {}

        @Override
        public void setArgs(String[] args) {
            playerName = args[0];
        }

        @Override
        public void execute() {
            NetworkManager.getInstance().setConnectionInfo("localhost", 8080);
            NetworkManager.getInstance().connect();
            RoomManager.getInstance().setClientName(playerName);
        }

        @Override
        public String dump() {
            try {
                RoomManager rm = RoomManager.getInstance();
                rm.gameController = new GameController(playerName, rm.p2pManager, new DummyUI(playerName));
                rm.p2pManager = new P2PManager(playerName, rm.gameController);
                rm.gameController.setP2PManager(rm.p2pManager);
            } catch (Exception e) {
                return "";
            }
            int port = RoomManager.getInstance().p2pManager.getListeningPort();
            String command = String.format("GG|CONNECT|%s|%d\n", playerName, port);
            return command;
        }
    }



    public static class CreateRoom implements Command {
        private String roomName;
        private int maxPlayers;
        private int maxRounds;

        public CreateRoom() {}

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
            RoomManager.getInstance().createRoom(roomName, maxPlayers, maxRounds);
        }

        @Override
        public String dump() {
            String command = String.format("GG|CREATE_ROOM|%s|%d|%d\n", roomName, maxPlayers, maxRounds);
            return command;
        }
    }



    public static class ListRoom implements Command {
        public ListRoom() {}

        @Override
        public void setArgs(String[] args) {}

        @Override
        public void execute() {}

        @Override
        public String dump() {
            String command = "GG|LIST_ROOMS\n";
            return command;
        }
    }



    public static class JoinRoom implements Command {
        private String room_name;

        public JoinRoom() {}

        @Override
        public void setArgs(String[] args) {
            try {
                room_name = args[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for JoinRoom command");
            }
        }

        @Override
        public void execute() {
        }

        @Override
        public String dump() {
            String command = String.format("GG|JOIN_ROOM|%s\n", room_name);
            return command;
        }
    }



    public static class LeaveRoom implements Command {
        private String room_name;

        public LeaveRoom() {}

        @Override
        public void setArgs(String[] args) {
            try {
                room_name = args[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for LeaveRoom command");
            }
        }

        @Override
        public void execute() {
            RoomManager.getInstance().leaveRoom();
        }

        @Override
        public String dump() {
            String command = String.format("GG|LEAVE_ROOM|%s\n", room_name);
            return command;
        }
    }



    public static class KickPlayer implements Command {
        private String room_name;
        private String player_name;

        public KickPlayer() {}

        @Override
        public void setArgs(String[] args) {
            try {
                room_name = args[0];
                player_name = args[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for KickPlayer command");
            }
        }

        @Override
        public void execute() {}

        @Override
        public String dump() {
            String command = String.format("GG|KICK_PLAYER|%s|%s\n", room_name, player_name);
            return command;
        }
    }



    public static class StartGame implements Command {
        private String room_name;

        public StartGame() {}

        @Override
        public void setArgs(String[] args) {
            try {
                room_name = args[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for StartGame command");
            }
        }

        @Override
        public void execute() {}

        @Override
        public String dump() {
            String command = String.format("GG|START_GAME|%s\n", room_name);
            return command;
        }
    }



    public static class PlayServer implements Command {
        private int max_rounds;

        public PlayServer() {}

        @Override
        public void setArgs(String[] args) {
            try {
                max_rounds = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for max_rounds");
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for PlayServer command");
            }
        }

        @Override
        public void execute() {}

        @Override
        public String dump() {
            String command = String.format("GG|PLAY_SERVER|%d\n", max_rounds);
            return command;
        }
    }






    public static class SetSecret implements Command {
        public SetSecret() {}

        private String[] secret;

        @Override
        public void setArgs(String[] args) {
            try {
                secret = args;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for StartGame command");
            }
        }

        @Override
        public void execute() {
            RoomManager.getInstance().gameController.setSecret(secret);
        }

        @Override
        public String dump() {
            return null;
        }
    }

    public static class Guess implements Command {
        public Guess() {}

        private String[] guess;

        @Override
        public void setArgs(String[] args) {
            try {
                guess = args;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for StartGame command");
            }
        }

        @Override
        public void execute() {
            RoomManager.getInstance().gameController.sendGuess(guess);
        }

        @Override
        public String dump() {
            return null;
        }
    }
}
