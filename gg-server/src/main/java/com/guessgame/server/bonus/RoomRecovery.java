package com.guessgame.server.bonus;

import com.guessgame.server.PlayerInfo;

import java.util.*;

/**
 * RoomRecovery.java
 * ------------------
 * Gère la récupération d'une salle de jeu en cas de déconnexion d'un joueur.
 *
 * Comportement :
 *   - Si un joueur quitte en cours de partie, les autres sont notifiés.
 *   - Si le joueur déconnecté était le détenteur du secret, la partie est annulée
 *     et les joueurs restants reçoivent GG|GAME_CANCELLED|raison.
 *   - Si la salle devient vide, elle est supprimée automatiquement.
 *
 * HYPOTHÈSE :
 *   - Il n'y a pas de rejoin en cours de partie (trop complexe sans état persistant).
 *   - Une partie annulée peut être redémarrée par les joueurs restants.
 */
public class RoomRecovery {

    /**
     * Notifie tous les joueurs d'une salle qu'un joueur s'est déconnecté.
     *
     * @param disconnectedPlayer Nom du joueur déconnecté
     * @param roomName           Nom de la salle affectée
     * @param remainingPlayers   Joueurs encore présents dans la salle
     * @param wasSecretHolder    Vrai si le joueur déconnecté détenait le secret
     */
    public static void handlePlayerDisconnect(
            String disconnectedPlayer,
            String roomName,
            List<PlayerInfo> remainingPlayers,
            boolean wasSecretHolder) {

        System.out.println("[RoomRecovery] Joueur déconnecté : " + disconnectedPlayer
                + " dans la salle : " + roomName);

        if (remainingPlayers.isEmpty()) {
            System.out.println("[RoomRecovery] Salle vide après déconnexion — suppression de : " + roomName);
            return;
        }

        // Notifier les joueurs restants
        String reason = wasSecretHolder
                ? disconnectedPlayer + " (détenteur du secret) s'est déconnecté"
                : disconnectedPlayer + " s'est déconnecté";

        String cancelMsg = "GG|PLAYER_LEFT|" + disconnectedPlayer + "|" + reason;
        String gameCancelMsg = wasSecretHolder
                ? "GG|GAME_CANCELLED|" + reason
                : null;

        for (PlayerInfo player : remainingPlayers) {
            try {
                player.send(cancelMsg);
                System.out.println("[RoomRecovery] Notifié : " + player.username);

                if (gameCancelMsg != null) {
                    player.send(gameCancelMsg);
                    System.out.println("[RoomRecovery] Partie annulée notifiée à : " + player.username);
                }
            } catch (Exception e) {
                System.err.println("[RoomRecovery] Erreur notification " + player.username
                        + " : " + e.getMessage());
            }
        }

        if (wasSecretHolder) {
            System.out.println("[RoomRecovery] Partie annulée dans la salle " + roomName
                    + " — le détenteur du secret s'est déconnecté.");
        }
    }

    /**
     * Vérifie si une salle est vide et doit être supprimée.
     *
     * @param remainingPlayers Joueurs encore dans la salle
     * @return true si la salle doit être supprimée
     */
    public static boolean shouldDeleteRoom(List<PlayerInfo> remainingPlayers) {
        return remainingPlayers == null || remainingPlayers.isEmpty();
    }
}
