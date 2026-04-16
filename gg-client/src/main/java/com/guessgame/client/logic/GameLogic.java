package com.guessgame.client.logic;

import java.util.Arrays;
import java.util.Random;

/**
 * GameLogic.java
 * ---------------
 * Contient toutes les règles du jeu Guess Game :
 *   - Génération/stockage de la combinaison secrète
 *   - Évaluation d'une proposition (couleurs correctes + positions correctes)
 *   - Détection de victoire / fin de partie
 *
 * HYPOTHÈSES :
 *   - Les couleurs valides sont : RED, GREEN, BLUE, YELLOW, ORANGE
 *   - La combinaison secrète est composée de 4 couleurs (répétitions permises)
 *   - "couleurs_correctes" dans le feedback = nombre total de couleurs présentes
 *     dans la combinaison secrète (positions confondues), incluant les bien placées.
 *   - "positions_correctes" = nombre de couleurs exactement bien placées.
 */
public class GameLogic {

    public static final String[] VALID_COLORS = {"RED", "GREEN", "BLUE", "YELLOW", "ORANGE"};
    public static final int COMBINATION_SIZE = 4;

    private String[] secretCombination;
    private int maxAttempts;
    private int currentAttempts;

    // -----------------------------------------------------------------------
    // Constructeur
    // -----------------------------------------------------------------------
    public GameLogic(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.currentAttempts = 0;
    }

    // -----------------------------------------------------------------------
    // Gestion de la combinaison secrète
    // -----------------------------------------------------------------------

    /** Génère une combinaison secrète aléatoire (utilisé pour le mode solo). */
    public void generateRandomSecret() {
        Random rand = new Random();
        secretCombination = new String[COMBINATION_SIZE];
        for (int i = 0; i < COMBINATION_SIZE; i++) {
            secretCombination[i] = VALID_COLORS[rand.nextInt(VALID_COLORS.length)];
        }
        System.out.println("[GameLogic] Combinaison secrète générée : " + Arrays.toString(secretCombination));
    }

    /** Permet au joueur détenteur de définir manuellement sa combinaison secrète. */
    public boolean setSecret(String[] combination) {
        if (!isValidCombination(combination)) {
            System.err.println("[GameLogic] Combinaison invalide : " + Arrays.toString(combination));
            return false;
        }
        this.secretCombination = combination.clone();
        System.out.println("[GameLogic] Combinaison secrète définie : " + Arrays.toString(secretCombination));
        return true;
    }

    // -----------------------------------------------------------------------
    // Évaluation d'une proposition
    // -----------------------------------------------------------------------

    /**
     * Évalue une proposition et retourne le feedback.
     *
     * @param guess La proposition du joueur (4 couleurs)
     * @return int[] { couleurs_correctes, positions_correctes }
     *
     * Algorithme :
     *   1. Première passe : trouver les positions exactes (bonne couleur, bonne place)
     *   2. Deuxième passe : trouver les couleurs présentes mais mal placées
     *   couleurs_correctes inclut les bien placées + les présentes ailleurs.
     */
    public int[] evaluateGuess(String[] guess) {
        if (secretCombination == null) {
            throw new IllegalStateException("[GameLogic] Aucune combinaison secrète définie.");
        }

        int correctPositions = 0;
        int correctColors    = 0;

        boolean[] secretUsed = new boolean[COMBINATION_SIZE];
        boolean[] guessUsed  = new boolean[COMBINATION_SIZE];

        // Passe 1 : positions exactes
        for (int i = 0; i < COMBINATION_SIZE; i++) {
            if (guess[i].equals(secretCombination[i])) {
                correctPositions++;
                secretUsed[i] = true;
                guessUsed[i]  = true;
            }
        }

        // Passe 2 : couleurs présentes mais mal placées
        for (int i = 0; i < COMBINATION_SIZE; i++) {
            if (guessUsed[i]) continue;
            for (int j = 0; j < COMBINATION_SIZE; j++) {
                if (!secretUsed[j] && guess[i].equals(secretCombination[j])) {
                    correctColors++;
                    secretUsed[j] = true;
                    break;
                }
            }
        }

        // Total couleurs correctes (bien placées + présentes ailleurs)
        correctColors += correctPositions;
        currentAttempts++;

        System.out.println("[GameLogic] Proposition évaluée :");
        System.out.println("  Proposition       : " + Arrays.toString(guess));
        System.out.println("  Couleurs correctes: " + correctColors);
        System.out.println("  Positions correctes: " + correctPositions);
        System.out.println("  Tentatives restantes: " + getRemainingAttempts());

        return new int[]{correctColors, correctPositions};
    }

    // -----------------------------------------------------------------------
    // Conditions de victoire / fin de partie
    // -----------------------------------------------------------------------

    /** Vrai si toutes les positions sont correctes (victoire). */
    public boolean isWin(int[] feedback) {
        return feedback[1] == COMBINATION_SIZE;
    }

    /** Vrai si le nombre maximum de tentatives est atteint. */
    public boolean isGameOver() {
        return currentAttempts >= maxAttempts;
    }

    // -----------------------------------------------------------------------
    // Réinitialisation
    // -----------------------------------------------------------------------

    /** Réinitialise l'état pour une nouvelle partie (NEW_GAME). */
    public void reset() {
        currentAttempts  = 0;
        secretCombination = null;
        System.out.println("[GameLogic] Jeu réinitialisé.");
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    /** Vérifie que chaque couleur de la combinaison est valide. */
    public static boolean isValidCombination(String[] combination) {
        if (combination == null || combination.length != COMBINATION_SIZE) return false;
        for (String color : combination) {
            if (!isValidColor(color)) return false;
        }
        return true;
    }

    /** Vérifie qu'une couleur individuelle est valide. */
    public static boolean isValidColor(String color) {
        for (String valid : VALID_COLORS) {
            if (valid.equalsIgnoreCase(color)) return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public int getRemainingAttempts() { return maxAttempts - currentAttempts; }
    public int getCurrentAttempts()   { return currentAttempts; }
    public int getMaxAttempts()       { return maxAttempts; }
    public String[] getSecretCombination() { return secretCombination; }
}
