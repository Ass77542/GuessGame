package com.guessgame.client.manager;

import java.util.Vector;

public class RoomManager {
    public class Room {
        public String name;
        public int maxPlayers;
        public int maxRounds;

        public Room(String name, int maxPlayers, int maxRounds) {
            this.name = name;
            this.maxPlayers = maxPlayers;
            this.maxRounds = maxRounds;
        }
    };

    private static RoomManager instance;

    private Vector<String> rooms = new Vector<>();
    private String currentRoom;

    private Room hostRoom;
    private boolean isHost;

    private RoomManager() {}

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public String createRoom(Room room) {
        if (hostRoom != null) {
            return "Already hosting a room.";
        }
        hostRoom = room;
        isHost = true;
        return null;
    }

    public void addRoom(String roomName) {
        if (rooms.contains(roomName)) {
            return;
        }
        rooms.add(roomName);
    }

    public void removeRoom(String roomName) {
        rooms.remove(roomName);
    }

    public boolean roomExists(String roomName) {
        return rooms.contains(roomName);
    }

    public Vector<String> getRooms() {
        return rooms;
    }

    public void joinRoom(String roomName) {
        if (!rooms.contains(roomName)) {
            throw new IllegalArgumentException("Room does not exist");
        }
        currentRoom = roomName;
        isHost = false;
    }

    public void leaveRoom() {
        currentRoom = null;
    }
};
