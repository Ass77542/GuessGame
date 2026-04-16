package com.guessgame.client.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GameState.java
 * ---------------
 * Suit l'état courant de la partie du point de vue d'un client local.
 *
 * HYPOTHÈSES :
 *   - Le premier joueur dans la liste reçue de GG|GAME_STARTED est le détenteur initial du secret.
 *   - À chaque NEW_GAME, le rôle de détenteur du secret tourne au joueur suivant dans la liste.
 *   - Un joueur ne peut pas deviner sa propre combinaison secrète.
 */
public class GameState {

    // -----------------------------------------------------------------------
    // États possibles
    // -----------------------------------------------------------------------
    public enum State {
        WAITING,          // Dans le lobby, partie non démarrée
        CHOOSING_SECRET,  // En attente que le détenteur choisisse la combinaison
        PLAYING,          // Partie en cours, les devinettes sont acceptées
        GAME_OVER         // Partie terminée (victoire ou tentatives épuisées)
    }

    private State  currentState;
    private String localPlayerName;
    private String roomName;
    private String secretHolder;       // Joueur qui possède la combinaison secrète
    private List<String> players;      // Tous les joueurs de la salle
    private int maxAttempts;
    private int currentAttempts;
    private boolean isLocalSecretHolder;

    // -----------------------------------------------------------------------
    // Constructeur
    // -----------------------------------------------------------------------
    public GameState(String localPlayerName) {
        this.localPlayerName = localPlayerName;
        this.players         = new ArrayList<>();
        this.currentState    = State.WAITING;
        this.currentAttempts = 0;
    }

    // -----------------------------------------------------------------------
    // Démarrage de partie
    // -----------------------------------------------------------------------

    /**
     * Initialise l'état lors de la réception de GG|GAME_STARTED.
     *
     * @param roomName    Nom de la salle
     * @param players     Liste des joueurs (ordre détermine qui est détenteur)
     * @param maxAttempts Nombre maximum de tentatives
     */
    public void startGame(String roomName, List<String> players, int maxAttempts) {
        this.roomName        = roomName;
        this.players         = new ArrayList<>(players);
        this.maxAttempts     = maxAttempts;
        this.currentAttempts = 0;
        this.currentState    = State.CHOOSING_SECRET;

        // Le premier joueur de la liste est le détenteur du secret
        this.secretHolder          = players.get(0);
        this.isLocalSecretHolder   = secretHolder.equals(localPlayerName);

        System.out.println("[GameState] Partie démarrée dans la salle : " + roomName);
        System.out.println("[GameState] Joueurs :");
        for (String p : players) System.out.println("  - " + p);
        System.out.println("[GameState] Détenteur du secret : " + secretHolder);
        System.out.println("[GameState] Je suis le détenteur : " + isLocalSecretHolder);
    }

    // -----------------------------------------------------------------------
    // Transitions d'état
    // -----------------------------------------------------------------------

    /** Appelé lorsque GG|SECRET_SET est reçu ou envoyé. */
    public void secretWasSet(String holderName) {
        this.secretHolder        = holderName;
        this.isLocalSecretHolder = holderName.equals(localPlayerName);
        this.currentState        = State.PLAYING;
        System.out.println("[GameState] Secret défini par : " + holderName + " → état PLAYING");
    }

    /** Incrémente le compteur de tentatives. */
    public void incrementAttempts() {
        currentAttempts++;
        System.out.println("[GameState] Tentative " + currentAttempts + "/" + maxAttempts);
    }

    /** Passe l'état en GAME_OVER. */
    public void setGameOver() {
        currentState = State.GAME_OVER;
        System.out.println("[GameState] État → GAME_OVER");
    }

    /**
     * Réinitialise pour une nouvelle partie (GG|NEW_GAME).
     * Le rôle de détenteur du secret tourne au joueur suivant.
     */
    public void reset() {
        currentAttempts = 0;
        currentState    = State.CHOOSING_SECRET;

        if (!players.isEmpty()) {
            int idx         = players.indexOf(secretHolder);
            int nextIdx     = (idx + 1) % players.size();
            secretHolder    = players.get(nextIdx);
            isLocalSecretHolder = secretHolder.equals(localPlayerName);
        }

        System.out.println("[GameState] Nouvelle partie. Nouveau détenteur du secret : " + secretHolder);
        System.out.println("[GameState] Je suis le détenteur : " + isLocalSecretHolder);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public State   getCurrentState()       { return currentState; }
    public String  getSecretHolder()       { return secretHolder; }
    public boolean isLocalSecretHolder()   { return isLocalSecretHolder; }
    public boolean isPlaying()             { return currentState == State.PLAYING; }
    public boolean isChoosingSecret()      { return currentState == State.CHOOSING_SECRET; }
    public boolean isGameOver()            { return currentState == State.GAME_OVER; }
    public int     getRemainingAttempts()  { return maxAttempts - currentAttempts; }
    public int     getCurrentAttempts()    { return currentAttempts; }
    public int     getMaxAttempts()        { return maxAttempts; }
    public List<String> getPlayers()       { return Collections.unmodifiableList(players); }
    public String  getLocalPlayerName()    { return localPlayerName; }
    public String  getRoomName()           { return roomName; }
}
