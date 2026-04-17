package com.guessgame.client.command;

import com.guessgame.client.manager.NetworkManager;
import com.guessgame.client.manager.RoomManager;

import com.guessgame.client.p2p.P2PManager;

public class ServerCommand {
    public static class Connect implements Command {
        private Class<? extends Response> responseClass = ServerResponse.Connected.class;
        Response response;

        private String player_name;

        public Connect() {}

        @Override
        public void setArgs(String[] args) {
            player_name = args[0];
        }

        @Override
        public void execute() {
            NetworkManager.getInstance().setConnectionInfo("localhost", 8080);
            NetworkManager.getInstance().connect();
            RoomManager.getInstance().setClientName(player_name);
        }

        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            try {
                RoomManager.getInstance().p2pManager = new P2PManager(player_name, null);
            } catch (Exception e) {
                return "";
            }
            int port = RoomManager.getInstance().p2pManager.getListeningPort();
            String command = String.format("GG|CONNECT|%s|%d\n", player_name, port);
            return command;
        }
    }



    public static class CreateRoom implements Command {
        private Class<? extends Response> responseClass = ServerResponse.RoomCreated.class;
        Response response;

        private String room_name;
        private int max_players;
        private int max_rounds;

        public CreateRoom() {}

        @Override
        public void setArgs(String[] args) {
            try {
                room_name = args[0];
                max_players = Integer.parseInt(args[1]);
                max_rounds = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for max_players or max_rounds");
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not enough arguments for CreateRoom command");
            }
        }

        @Override
        public void execute() {
            RoomManager.getInstance().createRoom(RoomManager.getInstance().new Room(room_name, max_players, max_rounds));
        }

        @Override
        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            String command = String.format("GG|CREATE_ROOM|%s|%d|%d\n", room_name, max_players, max_rounds);
            return command;
        }
    }



    public static class ListRoom implements Command {
        private Class<? extends Response> responseClass = ServerResponse.RoomList.class;
        Response response;

        public ListRoom() {}

        @Override
        public void setArgs(String[] args) {}

        @Override
        public void execute() {}

        @Override
        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            String command = "GG|LIST_ROOMS\n";
            return command;
        }
    }



    public static class JoinRoom implements Command {
        private Class<? extends Response> responseClass = ServerResponse.RoomJoined.class;
        Response response;

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
            RoomManager.getInstance().joinRoom(room_name);
        }

        @Override
        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            String command = String.format("GG|JOIN_ROOM|%s\n", room_name);
            return command;
        }
    }



    public static class LeaveRoom implements Command {
        private Class<? extends Response> responseClass = ServerResponse.RoomLeft.class;
        Response response;

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
        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            String command = String.format("GG|LEAVE_ROOM|%s\n", room_name);
            return command;
        }
    }



    public static class KickPlayer implements Command {
        private Class<? extends Response> responseClass = ServerResponse.PlayerKicked.class;
        Response response;

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
        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            String command = String.format("GG|KICK_PLAYER|%s|%s\n", room_name, player_name);
            return command;
        }
    }



    public static class StartGame implements Command {
        private Class<? extends Response> responseClass = ServerResponse.GameStarted.class;
        Response response;

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
        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            String command = String.format("GG|START_GAME|%s\n", room_name);
            return command;
        }
    }



    public static class PlayServer implements Command {
        private Class<? extends Response> responseClass = ServerResponse.ServerGameStarted.class;
        Response response;

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
        public void setResponse(Response response) {
            if (!responseClass.isInstance(response)) {
                throw new IllegalArgumentException("Invalid response type");
            }
            this.response = response;
        }

        @Override
        public String dump() {
            String command = String.format("GG|PLAY_SERVER|%d\n", max_rounds);
            return command;
        }
    }
    // Create other game commands




    //
    public static class GuessSecret implements Command {
        public GuessSecret() {}

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
            RoomManager.getInstance().getHostRoom().gameController.sendGuess(guess);
        }

        @Override
        public void setResponse(Response response) {
        }

        @Override
        public String dump() {
            return "";
        }
    }
}
