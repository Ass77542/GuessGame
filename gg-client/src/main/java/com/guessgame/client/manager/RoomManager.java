package com.guessgame.client.manager;

import java.util.Vector;

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
    public GameController gameController;

    public String roomName;
    public int maxPlayers;
    public int maxRounds;

    private boolean isHost;

    private static RoomManager instance;
    private RoomManager() {}

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void joinRoom(String name, int maxPlayers, int maxRounds) {
        this.roomName = name;
        this.maxPlayers = maxPlayers;
        this.maxRounds = maxRounds;
        isHost = false;
    }

    public void createRoom(String name, int maxPlayers, int maxRounds) {
        this.roomName = name;
        this.maxPlayers = maxPlayers;
        this.maxRounds = maxRounds;
        isHost = true;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String name) {
        this.clientName = name;
    }
};
