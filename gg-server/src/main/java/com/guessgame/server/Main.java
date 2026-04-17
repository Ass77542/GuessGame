package com.guessgame.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;

public class Main {

    // room -> joueurs
    private static Map<String, List<PlayerInfo>> rooms = new HashMap<>();

    // socket -> infos joueur
    private static Map<Socket, PlayerInfo> connectedPlayers = new HashMap<>();


    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(8080);

            System.out.println("Serveur lancé...");

            while (true) {
                Socket client = server.accept();
                System.out.println("Client connecté : " + client);
                new Thread(() -> handleClient(client)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void handleClient(Socket client) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));

            String message;

            while ((message = in.readLine()) != null) {
                System.out.println("Message reçu : " + message);
                if (!message.startsWith("GG|")) {
                    continue;
                }
                String[] parts = message.split("\\|");
                String command = parts[1];
                switch (command) {
                    case "CONNECT":
                        String username = parts[2];
                        int p2pPort = Integer.parseInt(parts[3]);
                        String ip = client.getInetAddress().getHostAddress();
                        PlayerInfo player = new PlayerInfo(
                                username,
                                ip,
                                p2pPort,
                                client
                                );
                        connectedPlayers.put(client, player);

                        System.out.println(
                                "CONNECT : "
                                + username
                                + " / "
                                + ip
                                + ":"
                                + p2pPort
                                );

                        break;

                    case "CREATE_ROOM":
                        String roomCreate = parts[2];
                        if (!rooms.containsKey(roomCreate)) {
                            rooms.put(roomCreate, new ArrayList<>());
                            PlayerInfo joueur = connectedPlayers.get(client);
                            rooms.get(roomCreate).add(joueur);
                            System.out.println(
                                    "CREATE_ROOM : "
                                    + roomCreate
                                    );
                        } else {
                            System.out.println(
                                    "Room déjà existante : "
                                    + roomCreate
                                    );
                        }
                        break;

                    case "JOIN_ROOM":
                        String roomJoin = parts[2];
                        if (rooms.containsKey(roomJoin)) {
                            PlayerInfo joueur = connectedPlayers.get(client);
                            rooms.get(roomJoin).add(joueur);
                            System.out.println(
                                    "JOIN_ROOM : "
                                    + roomJoin
                                    );
                        } else {
                            System.out.println(
                                    "Room inexistante : "
                                    + roomJoin
                                    );
                        }
                        break;

                    case "LEAVE_ROOM":
                        for (String room : rooms.keySet()) {
                            rooms.get(room).remove(
                                    connectedPlayers.get(client)
                                    );
                        }
                        System.out.println("LEAVE_ROOM");
                        break;

                    case "START_GAME":
                        String nomSalle = parts[2];
                        List<PlayerInfo> joueurs = rooms.get(nomSalle);

                        if (joueurs != null) {

                            StringBuilder liste = new StringBuilder();

                            for (PlayerInfo p : joueurs) {
                                if (liste.length() > 0) liste.append(",");

                                liste.append(
                                        p.username + ":" +
                                        p.ip + ":" +
                                        p.p2pPort
                                        );
                            }

                            String msg = "GG|GAME_STARTED|" + nomSalle + "|" + liste;

                            for (PlayerInfo p : joueurs) {
                                p.send(msg);
                            }

                            System.out.println("GAME_STARTED envoyé : " + msg);
                        }
                        break;

                    case "LIST_ROOMS":
                        System.out.println(
                                "LIST_ROOMS : "
                                + rooms.keySet()
                                );
                        break;

                    default:
                        System.out.println(
                                "Commande inconnue"
                                );
                        break;
                }
            }

        } catch (Exception e) {

            System.out.println("Client déconnecté");

            connectedPlayers.remove(client);
        }
    }
}
