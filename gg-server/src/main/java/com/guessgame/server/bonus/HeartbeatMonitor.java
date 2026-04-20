package com.guessgame.server.bonus;

import com.guessgame.server.PlayerInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HeartbeatMonitor.java
 * ----------------------
 * Surveille la vivacité de chaque client connecté.
 *
 * Mécanisme :
 *   - Chaque client doit envoyer GG|PING périodiquement.
 *   - Le serveur répond GG|PONG.
 *   - Si un client ne répond pas dans le délai TIMEOUT_MS, il est considéré
 *     comme déconnecté et son socket est fermé proprement.
 *
 * HYPOTHÈSE :
 *   - Le client envoie GG|PING toutes les 5 secondes.
 *   - Le serveur tolère jusqu'à 15 secondes sans activité avant de déclarer
 *     le client mort (3 PING manqués).
 */
public class HeartbeatMonitor {

    private static final long TIMEOUT_MS      = 15_000; // 15 secondes sans PING
    private static final long CHECK_INTERVAL  = 5_000;  // Vérification toutes les 5 sec

    // nom joueur → timestamp du dernier PING reçu
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();

    // Callback appelé quand un joueur est déclaré mort
    public interface DisconnectCallback {
        void onPlayerTimeout(String playerName);
    }

    private final DisconnectCallback callback;
    private Thread monitorThread;
    private volatile boolean running = false;

    public HeartbeatMonitor(DisconnectCallback callback) {
        this.callback = callback;
    }

    /** Enregistre l'activité d'un joueur (appelé à chaque GG|PING reçu). */
    public void recordPing(String playerName) {
        lastSeen.put(playerName, System.currentTimeMillis());
        System.out.println("[HeartbeatMonitor] PING reçu de : " + playerName);
    }

    /** Ajoute un joueur au monitoring (appelé à la connexion). */
    public void registerPlayer(String playerName) {
        lastSeen.put(playerName, System.currentTimeMillis());
        System.out.println("[HeartbeatMonitor] Joueur enregistré : " + playerName);
    }

    /** Retire un joueur du monitoring (appelé à la déconnexion propre). */
    public void unregisterPlayer(String playerName) {
        lastSeen.remove(playerName);
        System.out.println("[HeartbeatMonitor] Joueur retiré : " + playerName);
    }

    /** Démarre le thread de surveillance. */
    public void start() {
        running = true;
        monitorThread = new Thread(() -> {
            System.out.println("[HeartbeatMonitor] Surveillance démarrée.");
            while (running) {
                try {
                    Thread.sleep(CHECK_INTERVAL);
                    checkTimeouts();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("[HeartbeatMonitor] Surveillance arrêtée.");
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("HeartbeatMonitor");
        monitorThread.start();
    }

    /** Arrête proprement le monitoring. */
    public void stop() {
        running = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Vérifie si des joueurs ont dépassé le délai d'inactivité. */
    private void checkTimeouts() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : lastSeen.entrySet()) {
            String playerName = entry.getKey();
            long elapsed = now - entry.getValue();
            if (elapsed > TIMEOUT_MS) {
                System.out.println("[HeartbeatMonitor] TIMEOUT détecté pour : "
                        + playerName + " (inactif depuis " + elapsed / 1000 + "s)");
                lastSeen.remove(playerName);
                callback.onPlayerTimeout(playerName);
            }
        }
    }
}
