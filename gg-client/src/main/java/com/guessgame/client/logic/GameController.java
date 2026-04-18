package com.guessgame.client.logic;

import com.guessgame.client.p2p.P2PManager;
import java.util.Arrays;
import java.util.List;

/**
 * GameController.java
 * --------------------
 * Coordonnateur central qui relie la logique du jeu (GameLogic, GameState)
 * et la couche P2P (P2PManager).
 *
 * Responsabilités :
 *   - Réagir aux messages P2P entrants (implémente GameEventListener)
 *   - Déclencher les actions de jeu (setSecret, sendGuess, requestNewGame)
 *   - Notifier l'interface utilisateur via GameUIListener
 *
 * HYPOTHÈSES :
 *   - Un seul joueur est désigné détenteur du secret à la fois.
 *   - Le détenteur reçoit les GG|GUESS et diffuse GG|FEEDBACK à tous.
 *   - Si la partie est gagnée, le détenteur envoie GG|WINNER à tous.
 *   - Les autres joueurs ne peuvent pas deviner en même temps (pas de gestion de tour ici —
 *     laissée à l'équipe UI/client si nécessaire).
 */
public class GameController implements P2PManager.GameEventListener {

    // -----------------------------------------------------------------------
    // Interface vers l'UI
    // -----------------------------------------------------------------------

    /**
     * L'interface utilisateur (CLI ou GUI) implémente ce listener pour
     * réagir aux événements de jeu.
     */
    public interface GameUIListener {
        /** Appelé quand un feedback est reçu (pour les devineurs). */
        void onFeedbackReceived(int correctColors, int correctPositions);

        /** Appelé quand un gagnant est annoncé. */
        void onWinner(String winnerName);

        /** Appelé quand toutes les tentatives sont épuisées sans victoire. */
        void onGameOver(String[] secretCombination);

        /** Appelé au début d'une nouvelle partie. */
        void onNewGame(boolean isSecretHolder, String secretHolder);

        /** Appelé si le joueur local doit choisir la combinaison secrète. */
        void onSecretNeeded();

        /** Appelé si le joueur local doit deviner. */
        void onGuessNeeded(int remainingAttempts);

        /** Affiche un message informatif. */
        void displayMessage(String message);
    }

    // -----------------------------------------------------------------------
    // Attributs
    // -----------------------------------------------------------------------

    private GameLogic      gameLogic;
    private final GameState      gameState;
    private P2PManager     p2pManager;
    private final GameUIListener uiListener;

    // -----------------------------------------------------------------------
    // Constructeur
    // -----------------------------------------------------------------------

    public GameController(String playerName,
                          GameUIListener uiListener) {
        this.gameState   = new GameState(playerName);
        this.uiListener  = uiListener;
    }

    public GameController(String playerName,
                          P2PManager p2pManager,
                          GameUIListener uiListener) {
        this.gameState   = new GameState(playerName);
        this.p2pManager  = p2pManager;
        this.uiListener  = uiListener;
    }

    public void setP2PManager(P2PManager manager) {
        this.p2pManager = manager;
    }

    // -----------------------------------------------------------------------
    // Démarrage de partie (appelé à la réception de GG|GAME_STARTED)
    // -----------------------------------------------------------------------

    /**
     * Initialise une nouvelle partie.
     *
     * @param roomName    Nom de la salle
     * @param players     Liste des joueurs au format "nom:ip:port"
     * @param maxAttempts Nombre maximum de tentatives
     */
    public void startGame(String roomName, List<String> players, int maxAttempts) {
        // Extraire les noms uniquement pour GameState
        List<String> playerNames = players.stream()
                .map(e -> e.split(":")[0])
                .collect(java.util.stream.Collectors.toList());

        this.gameLogic = new GameLogic(maxAttempts);
        gameState.startGame(roomName, playerNames, maxAttempts);

        // Établir les connexions P2P
        p2pManager.connectToPlayers(players);

        if (gameState.isLocalSecretHolder()) {
            uiListener.onSecretNeeded();
        } else {
            uiListener.displayMessage("En attente que " + gameState.getSecretHolder()
                    + " choisisse la combinaison secrète...");
        }
    }

    // -----------------------------------------------------------------------
    // Actions du joueur local
    // -----------------------------------------------------------------------

    /**
     * Le joueur détenteur définit sa combinaison secrète.
     * Envoie GG|SECRET_SET à tous les pairs.
     *
     * @param colors Tableau de 4 couleurs valides
     */
    public void setSecret(String[] colors) {
        if (!gameState.isLocalSecretHolder()) {
            uiListener.displayMessage("[ERREUR] Vous n'êtes pas le détenteur du secret.");
            return;
        }
        if (!GameLogic.isValidCombination(colors)) {
            uiListener.displayMessage("[ERREUR] Combinaison invalide. Couleurs valides : "
                    + Arrays.toString(GameLogic.VALID_COLORS));
            return;
        }

        gameLogic.setSecret(colors);
        gameState.secretWasSet(gameState.getLocalPlayerName());

        String msg = "GG|SECRET_SET|" + gameState.getLocalPlayerName();
        p2pManager.sendToAll(msg);
        uiListener.displayMessage("Combinaison secrète définie ! En attente des propositions...");
    }

    /**
     * Un devineur envoie une proposition au détenteur du secret.
     * Envoie GG|GUESS au détenteur uniquement.
     *
     * @param colors Tableau de 4 couleurs
     */
    public void sendGuess(String[] colors) {
        if (!gameState.isPlaying()) {
            uiListener.displayMessage("[ERREUR] La partie n'est pas en cours.");
            return;
        }
        if (gameState.isLocalSecretHolder()) {
            uiListener.displayMessage("[ERREUR] Vous êtes le détenteur du secret, vous ne pouvez pas deviner.");
            return;
        }
        if (!GameLogic.isValidCombination(colors)) {
            uiListener.displayMessage("[ERREUR] Combinaison invalide. Couleurs valides : "
                    + Arrays.toString(GameLogic.VALID_COLORS));
            return;
        }
        if (gameState.getRemainingAttempts() <= 0) {
            uiListener.displayMessage("[ERREUR] Plus de tentatives disponibles.");
            return;
        }

        String msg = "GG|GUESS|" + String.join("|", colors);
        p2pManager.sendTo(gameState.getSecretHolder(), msg);
        System.out.println("[GameController] Proposition envoyée à " + gameState.getSecretHolder()
                + " : " + Arrays.toString(colors));
    }

    /**
     * Demande une nouvelle partie.
     * Envoie GG|NEW_GAME à tous les pairs.
     */
    public void requestNewGame() {
        gameLogic.reset();
        gameState.reset();
        p2pManager.sendToAll("GG|NEW_GAME");

        boolean isHolder = gameState.isLocalSecretHolder();
        uiListener.onNewGame(isHolder, gameState.getSecretHolder());

        if (isHolder) {
            uiListener.onSecretNeeded();
        } else {
            uiListener.displayMessage("Nouvelle partie ! En attente que "
                    + gameState.getSecretHolder() + " choisisse le secret...");
        }
    }

    // -----------------------------------------------------------------------
    // Implémentation de GameEventListener (messages reçus depuis P2P)
    // -----------------------------------------------------------------------

    /**
     * Reçoit une proposition → calcule le feedback → diffuse à tous.
     * Appelé uniquement si le joueur local est le détenteur du secret.
     */
    @Override
    public void onGuessReceived(String fromPlayer, String[] colors) {
        if (!gameState.isLocalSecretHolder()) {
            System.err.println("[GameController] Reçu GUESS mais je ne suis pas détenteur !");
            return;
        }

        int[] feedback = gameLogic.evaluateGuess(colors);
        gameState.incrementAttempts();

        // Diffuser le feedback à tous les joueurs
        String feedbackMsg = "GG|FEEDBACK|" + feedback[0] + "|" + feedback[1];
        p2pManager.sendToAll(feedbackMsg);

        // Vérifier victoire
        if (gameLogic.isWin(feedback)) {
            String winMsg = "GG|WINNER|" + fromPlayer;
            p2pManager.sendToAll(winMsg);
            gameState.setGameOver();
            uiListener.onWinner(fromPlayer);
        }
        // Vérifier épuisement des tentatives
        else if (gameLogic.isGameOver()) {
            gameState.setGameOver();
            uiListener.onGameOver(gameLogic.getSecretCombination());
            uiListener.displayMessage("Partie terminée ! La combinaison était : "
                    + Arrays.toString(gameLogic.getSecretCombination()));
        }
    }

    /**
     * Reçoit le feedback du détenteur → notifie l'UI.
     * Appelé pour les joueurs qui devinent.
     */
    @Override
    public void onFeedbackReceived(String fromPlayer, int correctColors, int correctPositions) {
        gameState.incrementAttempts();
        uiListener.onFeedbackReceived(correctColors, correctPositions);

        if (correctPositions == GameLogic.COMBINATION_SIZE) {
            // On a deviné (le WINNER sera envoyé par le détenteur)
            return;
        }

        if (gameState.getRemainingAttempts() <= 0) {
            gameState.setGameOver();
            uiListener.onGameOver(null); // On ne connaît pas le secret
        } else {
            uiListener.onGuessNeeded(gameState.getRemainingAttempts());
        }
    }

    /**
     * Reçoit l'annonce d'un gagnant.
     */
    @Override
    public void onWinnerAnnounced(String winnerName) {
        System.out.println("[GameController] Gagnant annoncé : " + winnerName);
        gameState.setGameOver();
        uiListener.onWinner(winnerName);
    }

    /**
     * Reçoit la notification que le secret a été défini.
     * Transition vers l'état PLAYING.
     */
    @Override
    public void onSecretSet(String playerName) {
        System.out.println("[GameController] Secret défini par : " + playerName);
        gameState.secretWasSet(playerName);

        if (!gameState.isLocalSecretHolder()) {
            uiListener.displayMessage(playerName + " a choisi la combinaison secrète. À vous de deviner !");
            uiListener.onGuessNeeded(gameState.getRemainingAttempts());
        }
    }

    /**
     * Reçoit une demande de nouvelle partie.
     */
    @Override
    public void onNewGame() {
        System.out.println("[GameController] Nouvelle partie reçue.");
        gameLogic.reset();
        gameState.reset();

        boolean isHolder = gameState.isLocalSecretHolder();
        uiListener.onNewGame(isHolder, gameState.getSecretHolder());

        if (isHolder) {
            uiListener.onSecretNeeded();
        } else {
            uiListener.displayMessage("Nouvelle partie ! En attente que "
                    + gameState.getSecretHolder() + " choisisse le secret...");
        }
    }

    /**
     * Un pair s'est déconnecté.
     */
    @Override
    public void onPlayerDisconnected(String playerName) {
        System.out.println("[GameController] Joueur déconnecté : " + playerName);
        uiListener.displayMessage("[INFO] Le joueur " + playerName + " s'est déconnecté.");
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public GameState getGameState() { return gameState; }
    public GameLogic getGameLogic() { return gameLogic; }
}
