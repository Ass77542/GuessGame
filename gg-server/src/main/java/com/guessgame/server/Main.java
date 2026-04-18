package com.guessgame.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;

class Room {
    public int maxPlayers;
    public int maxRounds;
    public List<PlayerInfo> players;

    public Room() {
        this.players = new ArrayList<>();
    }

    public Room(int maxPlayers, int maxRounds) {
        this.maxPlayers = maxPlayers;
        this.maxRounds = maxRounds;
        this.players = new ArrayList<>();
    }
};

public class Main {

    // room -> joueurs
    private static Map<String, Room> rooms = new HashMap<>();

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
                        int maxPlayers = Integer.parseInt(parts[3]);
                        int maxRounds = Integer.parseInt(parts[4]);
                        if (!rooms.containsKey(roomCreate)) {
                            rooms.put(roomCreate, new Room(maxPlayers, maxRounds));
                            PlayerInfo joueur = connectedPlayers.get(client);
                            rooms.get(roomCreate).players.add(joueur);

                            StringBuilder sb = new StringBuilder();
                            sb.append("GG|ROOM_CREATED|").append(roomCreate);
                            joueur.send(sb.toString());
                            System.out.println(String.format("CREATE_ROOM : %s (%s)", roomCreate, sb.toString()));

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
                            Room room = rooms.get(roomJoin);
                            room.players.add(joueur);

                            String response = String.format("GG|JOINED_ROOM|%s|%d|%d", roomJoin, room.maxPlayers, room.maxRounds);
                            joueur.send(response);

                            System.out.println("JOIN_ROOM : " + roomJoin);
                        } else {
                            System.out.println(
                                    "Room inexistante : "
                                    + roomJoin
                                    );
                        }
                        break;

                    case "LEAVE_ROOM":
                        PlayerInfo joueur = connectedPlayers.get(client);
                        for (String room : rooms.keySet()) {
                            Room roomInstance = rooms.get(room);
                            roomInstance.players.remove(connectedPlayers.get(client));
                            String response = String.format("GG|LEFT_ROOM|%s\n", room);
                            joueur.send(response);
                        }
                        System.out.println("LEAVE_ROOM");
                        break;

                    case "START_GAME":
                        String nomSalle = parts[2];
                        List<PlayerInfo> joueurs = rooms.get(nomSalle).players;

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
