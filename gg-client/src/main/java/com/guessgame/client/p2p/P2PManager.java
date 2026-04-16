package com.guessgame.client.p2p;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * P2PManager.java
 * ----------------
 * Gère toutes les connexions pair-à-pair (P2P) du client local.
 *
 * Rôle :
 *   - Ouvre un ServerSocket sur un port aléatoire disponible (pour recevoir des connexions)
 *   - Permet de se connecter activement aux autres joueurs
 *   - Envoie des messages à un pair spécifique ou à tous les pairs
 *   - Délègue les messages reçus au GameController via l'interface GameEventListener
 *
 * HYPOTHÈSES :
 *   - Chaque client écoute sur un port assigné aléatoirement par l'OS.
 *   - Le port d'écoute est communiqué au serveur lors du CONNECT (ou dans une extension du protocole).
 *   - La liste des joueurs dans GG|GAME_STARTED est au format "nom:ip:port" pour chaque joueur.
 *   - Pour éviter les connexions doubles, seul le joueur dont le nom est
 *     lexicographiquement inférieur initie la connexion (l'autre accepte).
 *   - Le premier message envoyé sur une connexion sortante est GG|IDENTIFY|nom_local
 *     pour que le pair entrant puisse nous identifier.
 */
public class P2PManager {

    // -----------------------------------------------------------------------
    // Interface d'événements de jeu
    // -----------------------------------------------------------------------

    /**
     * Interface implémentée par GameController pour réagir aux messages P2P reçus.
     */
    public interface GameEventListener {
        void onGuessReceived(String fromPlayer, String[] colors);
        void onFeedbackReceived(String fromPlayer, int correctColors, int correctPositions);
        void onWinnerAnnounced(String winnerName);
        void onSecretSet(String playerName);
        void onNewGame();
        void onPlayerDisconnected(String playerName);
    }

    // -----------------------------------------------------------------------
    // Attributs
    // -----------------------------------------------------------------------

    private final ServerSocket serverSocket;
    private final int listeningPort;
    private final String localPlayerName;
    private final Map<String, P2PConnectionHandler> peers; // nom → handler
    private final GameEventListener listener;
    private Thread acceptThread;

    // -----------------------------------------------------------------------
    // Constructeur
    // -----------------------------------------------------------------------

    /**
     * @param localPlayerName Nom du joueur local (utilisé pour l'identification)
     * @param listener        Listener qui traite les événements de jeu
     * @throws IOException Si le ServerSocket ne peut pas être créé
     */
    public P2PManager(String localPlayerName, GameEventListener listener) throws IOException {
        this.localPlayerName = localPlayerName;
        this.listener        = listener;
        this.peers           = new ConcurrentHashMap<>();
        this.serverSocket    = new ServerSocket(0); // Port aléatoire libre
        this.listeningPort   = serverSocket.getLocalPort();
        System.out.println("[P2PManager] En écoute sur le port : " + listeningPort);
    }

    // -----------------------------------------------------------------------
    // Démarrage du serveur d'acceptation
    // -----------------------------------------------------------------------

    /**
     * Lance le thread d'acceptation des connexions entrantes.
     * Doit être appelé dès que le client démarre.
     */
    public void startAccepting() {
        acceptThread = new Thread(() -> {
            System.out.println("[P2PManager] Thread d'acceptation démarré.");
            while (!serverSocket.isClosed()) {
                try {
                    Socket incoming = serverSocket.accept();
                    System.out.println("[P2PManager] Connexion entrante de : "
                            + incoming.getInetAddress().getHostAddress());
                    // Connexion entrante : on ne connaît pas encore le nom du pair
                    P2PConnectionHandler handler =
                            new P2PConnectionHandler(incoming, listener, peers);
                    handler.start();
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("[P2PManager] Erreur d'acceptation : " + e.getMessage());
                    }
                }
            }
            System.out.println("[P2PManager] Thread d'acceptation arrêté.");
        });
        acceptThread.setDaemon(true);
        acceptThread.setName("P2P-Accept");
        acceptThread.start();
    }

    // -----------------------------------------------------------------------
    // Connexion aux autres joueurs
    // -----------------------------------------------------------------------

    /**
     * Se connecte à un pair distant.
     * Pour éviter les doublons, on n'initie la connexion que si
     * notre nom est lexicographiquement inférieur à celui du pair.
     *
     * @param peerName Nom du joueur distant
     * @param ip       Adresse IP du joueur distant
     * @param port     Port d'écoute P2P du joueur distant
     */
    public void connectToPeer(String peerName, String ip, int port) {
        // Éviter de se connecter à soi-même
        if (peerName.equals(localPlayerName)) return;

        // Éviter les connexions doubles : seul le "plus petit" nom initie
        if (localPlayerName.compareTo(peerName) >= 0) {
            System.out.println("[P2PManager] En attente que " + peerName + " se connecte à nous.");
            return;
        }

        Thread connectThread = new Thread(() -> {
            try {
                System.out.println("[P2PManager] Connexion vers " + peerName + " (" + ip + ":" + port + ")...");
                Socket socket = new Socket(ip, port);
                P2PConnectionHandler handler =
                        new P2PConnectionHandler(socket, peerName, listener, peers);
                peers.put(peerName, handler);
                handler.start();

                // S'identifier auprès du pair
                handler.sendMessage("GG|IDENTIFY|" + localPlayerName);
                System.out.println("[P2PManager] Connecté à : " + peerName);
            } catch (IOException e) {
                System.err.println("[P2PManager] Échec connexion à " + peerName + " : " + e.getMessage());
            }
        });
        connectThread.setDaemon(true);
        connectThread.setName("P2P-Connect-" + peerName);
        connectThread.start();
    }

    /**
     * Traite la liste de joueurs reçue dans GG|GAME_STARTED et établit les connexions.
     * Format attendu pour chaque joueur : "nom:ip:port"
     *
     * @param playerEntries Liste au format ["nom:ip:port", ...]
     */
    public void connectToPlayers(List<String> playerEntries) {
        System.out.println("[P2PManager] Établissement des connexions P2P...");
        for (String entry : playerEntries) {
            String[] parts = entry.split(":");
            if (parts.length == 3) {
                String name = parts[0];
                String ip   = parts[1];
                int    port = Integer.parseInt(parts[2]);
                System.out.println("[P2PManager] Joueur : " + name + " | IP : " + ip + " | Port : " + port);
                connectToPeer(name, ip, port);
            } else {
                System.err.println("[P2PManager] Format de joueur invalide : " + entry);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Envoi de messages
    // -----------------------------------------------------------------------

    /** Envoie un message à tous les pairs connectés. */
    public void sendToAll(String message) {
        System.out.println("[P2PManager] Broadcast → " + message);
        for (Map.Entry<String, P2PConnectionHandler> entry : peers.entrySet()) {
            entry.getValue().sendMessage(message);
        }
    }

    /** Envoie un message à un pair spécifique. */
    public void sendTo(String peerName, String message) {
        P2PConnectionHandler handler = peers.get(peerName);
        if (handler != null) {
            handler.sendMessage(message);
        } else {
            System.err.println("[P2PManager] Aucune connexion avec : " + peerName);
        }
    }

    // -----------------------------------------------------------------------
    // Arrêt propre
    // -----------------------------------------------------------------------

    /**
     * Ferme toutes les connexions et arrête le thread d'acceptation.
     */
    public void shutdown() {
        System.out.println("[P2PManager] Arrêt du gestionnaire P2P...");

        // Fermer tous les pairs
        for (P2PConnectionHandler handler : peers.values()) {
            handler.close();
        }
        peers.clear();

        // Fermer le serveur d'acceptation
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("[P2PManager] Erreur fermeture serverSocket : " + e.getMessage());
        }

        // Attendre la fin du thread d'acceptation
        if (acceptThread != null) {
            try {
                acceptThread.join(3000);
                System.out.println("[P2PManager] Thread d'acceptation terminé.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[P2PManager] Interruption lors du join.");
            }
        }

        System.out.println("[P2PManager] Arrêt complet.");
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public int getListeningPort() { return listeningPort; }
    public int getPeerCount()     { return peers.size(); }
    public Map<String, P2PConnectionHandler> getPeers() { return Collections.unmodifiableMap(peers); }
}
