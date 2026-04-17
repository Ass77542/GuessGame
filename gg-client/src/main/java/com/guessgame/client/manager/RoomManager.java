package com.guessgame.client.manager;

import java.util.Vector;

import com.guessgame.client.logic.GameLogic;
import com.guessgame.client.logic.GameController;
import com.guessgame.client.p2p.P2PManager;

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


public class RoomManager {
    private String clientName;
    public P2PManager p2pManager;

    public class Room {
        public String name;
        public int maxPlayers;
        public int maxRounds;
        public GameLogic gameLogic;
        public GameController gameController;

        public Room(String name, int maxPlayers, int maxRounds) {
            this.name = name;
            this.maxPlayers = maxPlayers;
            this.maxRounds = maxRounds;
            gameLogic = new GameLogic(maxRounds);

            try {
                // TODO change by real name
                p2pManager = new P2PManager("oui", null);
                p2pManager.startAccepting();
            } catch (Exception e) {
                throw new RuntimeException("Failed to init P2P manager");
            }
            gameController = new GameController("oui", p2pManager, new DummyUI("oui") {

            });





        }

        public void initClient() {

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

    public Room getHostRoom() {
        return hostRoom;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String name) {
        this.clientName = name;
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

    public void clearRoom() {
        rooms.clear();
    }

    public boolean roomExists(String roomName) {
        return rooms.contains(roomName);
    }

    public Vector<String> getRooms() {
        return rooms;
    }

    public void joinRoom(String roomName) {
        if (!rooms.contains(roomName)) {
            //throw new IllegalArgumentException("Room does not exist");
        }
        currentRoom = roomName;
        isHost = false;
    }

    public void leaveRoom() {
        currentRoom = null;
    }
};
