package com.guessgame.client.bonus;

import com.guessgame.client.manager.NetworkManager;

/**
 * ReconnectionManager.java
 * -------------------------
 * Gère la reconnexion automatique au serveur en cas de perte de connexion.
 *
 * Comportement :
 *   - Si le client perd la connexion avec le serveur, il tente de se reconnecter
 *     automatiquement jusqu'à MAX_RETRIES fois avec un délai croissant.
 *   - Si toutes les tentatives échouent, l'utilisateur est informé.
 *
 * HYPOTHÈSE :
 *   - Le client mémorise son nom d'utilisateur pour se ré-identifier automatiquement
 *     après reconnexion (renvoie GG|CONNECT|nom automatiquement).
 *   - Le délai de base entre chaque tentative est de 2 secondes, doublé à chaque échec.
 */
public class ReconnectionManager {

    private static final int  MAX_RETRIES     = 5;
    private static final long BASE_DELAY_MS   = 2000; // 2 secondes de base

    private final String serverIp;
    private final int    serverPort;
    private final String playerName;

    private int     retryCount  = 0;
    private boolean reconnecting = false;

    public interface ReconnectionListener {
        void onReconnecting(int attempt, int max);
        void onReconnected();
        void onReconnectionFailed();
    }

    private final ReconnectionListener listener;

    public ReconnectionManager(String serverIp, int serverPort,
                                String playerName, ReconnectionListener listener) {
        this.serverIp   = serverIp;
        this.serverPort = serverPort;
        this.playerName = playerName;
        this.listener   = listener;
    }

    /**
     * Démarre la tentative de reconnexion dans un thread séparé.
     * Appelé quand une IOException est détectée sur la connexion principale.
     */
    public synchronized void startReconnection() {
        if (reconnecting) {
            System.out.println("[ReconnectionManager] Reconnexion déjà en cours...");
            return;
        }
        reconnecting = true;
        retryCount   = 0;

        Thread reconnectThread = new Thread(() -> {
            System.out.println("[ReconnectionManager] Démarrage des tentatives de reconnexion...");

            while (retryCount < MAX_RETRIES) {
                retryCount++;
                long delay = BASE_DELAY_MS * retryCount; // délai croissant

                listener.onReconnecting(retryCount, MAX_RETRIES);
                System.out.println("[ReconnectionManager] Tentative " + retryCount
                        + "/" + MAX_RETRIES + " dans " + delay / 1000 + "s...");

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                try {
                    // Tenter de se reconnecter
                    NetworkManager nm = NetworkManager.getInstance();
                    nm.setConnectionInfo(serverIp, serverPort);
                    nm.connect();

                    // Renvoyer le message CONNECT pour se ré-identifier
                    String connectMsg = "GG|CONNECT|" + playerName + "\n";
                    nm.sendCommand(connectMsg);

                    System.out.println("[ReconnectionManager] Reconnexion réussie !");
                    reconnecting = false;
                    listener.onReconnected();
                    return;

                } catch (Exception e) {
                    System.err.println("[ReconnectionManager] Échec tentative "
                            + retryCount + " : " + e.getMessage());
                }
            }

            // Toutes les tentatives ont échoué
            System.err.println("[ReconnectionManager] Reconnexion abandonnée après "
                    + MAX_RETRIES + " tentatives.");
            reconnecting = false;
            listener.onReconnectionFailed();
        });

        reconnectThread.setDaemon(true);
        reconnectThread.setName("ReconnectionManager");
        reconnectThread.start();
    }

    public boolean isReconnecting() { return reconnecting; }
    public int     getRetryCount()  { return retryCount; }
}
