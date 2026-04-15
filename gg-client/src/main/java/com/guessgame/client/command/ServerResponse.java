package com.guessgame.client.command;

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
        public void execute() {}
    };



    public static class RoomList implements Response {
        public RoomList(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
    }



    public static class RoomJoined implements Response {
        public RoomJoined(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
    };



     public static class RoomLeft implements Response {
        public RoomLeft(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
    };



     public static class PlayerKicked implements Response {
        public PlayerKicked(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
     }



     public static class GameStarted implements Response {
        public GameStarted(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
     };



     public static class ServerGameStarted implements Response {
        public ServerGameStarted(/* let empty */) {}

        @Override
        public void setArgs(String[] args) {}
        @Override
        public void execute() {}
     };
}
