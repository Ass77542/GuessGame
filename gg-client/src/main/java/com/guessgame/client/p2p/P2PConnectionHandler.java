package com.guessgame.client.p2p;

import java.io.*;
import java.net.*;
import java.util.Map;

/**
 * P2PConnectionHandler.java
 * --------------------------
 * Gère la connexion TCP avec un seul pair (peer) dans un thread dédié.
 * Lit les messages entrants en continu et les délègue au listener.
 *
 * HYPOTHÈSES :
 *   - Le premier message envoyé par un pair est GG|IDENTIFY|nom_joueur
 *     (pour les connexions entrantes, afin de connaître l'identité du pair).
 *   - Les messages sont séparés par '\n' (PrintWriter avec auto-flush).
 *   - Le protocole P2P utilise les mêmes préfixes "GG|" que le protocole serveur.
 */
public class P2PConnectionHandler extends Thread {

    private final Socket  socket;
    private PrintWriter   out;
    private BufferedReader in;
    private String        peerName;   // Nom du joueur distant
    private final P2PManager.GameEventListener listener;
    private final Map<String, P2PConnectionHandler> peersMap;

    // -----------------------------------------------------------------------
    // Constructeurs
    // -----------------------------------------------------------------------

    /**
     * Connexion sortante : on connaît déjà le nom du pair.
     */
    public P2PConnectionHandler(Socket socket,
                                 String peerName,
                                 P2PManager.GameEventListener listener,
                                 Map<String, P2PConnectionHandler> peersMap) throws IOException {
        this.socket    = socket;
        this.peerName  = peerName;
        this.listener  = listener;
        this.peersMap  = peersMap;
        initStreams();
        setDaemon(true);
        setName("P2P-" + (peerName != null ? peerName : "unknown"));
    }

    /**
     * Connexion entrante : le nom du pair sera connu après réception de GG|IDENTIFY.
     */
    public P2PConnectionHandler(Socket socket,
                                 P2PManager.GameEventListener listener,
                                 Map<String, P2PConnectionHandler> peersMap) throws IOException {
        this(socket, null, listener, peersMap);
    }

    private void initStreams() throws IOException {
        this.out = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        this.in  = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
    }

    // -----------------------------------------------------------------------
    // Thread principal : lecture des messages entrants
    // -----------------------------------------------------------------------

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[P2P] ← Reçu de [" + (peerName != null ? peerName : "?") + "] : " + line);
                handleMessage(line);
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("[P2P] Erreur de connexion avec " + peerName + " : " + e.getMessage());
            }
        } finally {
            if (peerName != null) {
                peersMap.remove(peerName);
                listener.onPlayerDisconnected(peerName);
            }
            close();
        }
    }

    // -----------------------------------------------------------------------
    // Traitement des messages reçus
    // -----------------------------------------------------------------------

    private void handleMessage(String raw) {
        String[] parts = raw.trim().split("\\|");

        if (parts.length < 2 || !parts[0].equals("GG")) {
            System.err.println("[P2P] Format invalide ignoré : " + raw);
            return;
        }

        String msgType = parts[1];
        System.out.println("[P2P] Type du message : " + msgType);

        switch (msgType) {

            // ------------------------------------------------------------------
            // IDENTIFY : premier message d'une connexion entrante
            // ------------------------------------------------------------------
            case "IDENTIFY":
                if (parts.length >= 3) {
                    this.peerName = parts[2];
                    peersMap.put(peerName, this);
                    setName("P2P-" + peerName);
                    System.out.println("[P2P] Pair identifié : " + peerName);
                }
                break;

            // ------------------------------------------------------------------
            // SECRET_SET : un joueur annonce qu'il a choisi la combinaison secrète
            // ------------------------------------------------------------------
            case "SECRET_SET":
                if (parts.length >= 3) {
                    System.out.println("[P2P] Champ nom_joueur : " + parts[2]);
                    listener.onSecretSet(parts[2]);
                }
                break;

            // ------------------------------------------------------------------
            // GUESS : proposition de devinette envoyée au détenteur du secret
            // ------------------------------------------------------------------
            case "GUESS":
                if (parts.length >= 6) {
                    String[] colors = {parts[2], parts[3], parts[4], parts[5]};
                    System.out.println("[P2P] Champ couleur1 : " + parts[2]);
                    System.out.println("[P2P] Champ couleur2 : " + parts[3]);
                    System.out.println("[P2P] Champ couleur3 : " + parts[4]);
                    System.out.println("[P2P] Champ couleur4 : " + parts[5]);
                    listener.onGuessReceived(peerName, colors);
                } else {
                    System.err.println("[P2P] GUESS mal formé : " + raw);
                }
                break;

            // ------------------------------------------------------------------
            // FEEDBACK : réponse du détenteur du secret
            // ------------------------------------------------------------------
            case "FEEDBACK":
                if (parts.length >= 4) {
                    int correctColors    = Integer.parseInt(parts[2]);
                    int correctPositions = Integer.parseInt(parts[3]);
                    System.out.println("[P2P] Champ couleurs_correctes  : " + correctColors);
                    System.out.println("[P2P] Champ positions_correctes : " + correctPositions);
                    listener.onFeedbackReceived(peerName, correctColors, correctPositions);
                } else {
                    System.err.println("[P2P] FEEDBACK mal formé : " + raw);
                }
                break;

            // ------------------------------------------------------------------
            // WINNER : un joueur a trouvé la combinaison secrète
            // ------------------------------------------------------------------
            case "WINNER":
                if (parts.length >= 3) {
                    System.out.println("[P2P] Champ nom_joueur (gagnant) : " + parts[2]);
                    listener.onWinnerAnnounced(parts[2]);
                }
                break;

            // ------------------------------------------------------------------
            // NEW_GAME : demande de nouvelle partie
            // ------------------------------------------------------------------
            case "NEW_GAME":
                System.out.println("[P2P] Nouvelle partie demandée.");
                listener.onNewGame();
                break;

            default:
                System.out.println("[P2P] Type inconnu ignoré : " + msgType);
        }
    }

    // -----------------------------------------------------------------------
    // Envoi de messages
    // -----------------------------------------------------------------------

    public void sendMessage(String message) {
        System.out.println("[P2P] → Envoi à [" + peerName + "] : " + message);
        out.println(message);
    }

    // -----------------------------------------------------------------------
    // Fermeture
    // -----------------------------------------------------------------------

    public void close() {
        try {
            socket.close();
            System.out.println("[P2P] Connexion fermée avec : " + peerName);
        } catch (IOException e) {
            System.err.println("[P2P] Erreur à la fermeture : " + e.getMessage());
        }
    }

    public String getPeerName() { return peerName; }
}
