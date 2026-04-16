package com.guessgame.client.logic;

import java.util.Arrays;

/**
 * TestGameLogic.java
 * -------------------
 * Tests unitaires manuels pour GameLogic.
 * Lance avec : java com.guessgame.client.logic.TestGameLogic
 *
 * Aucune dépendance réseau — teste uniquement la logique de jeu.
 */
public class TestGameLogic {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  TESTS UNITAIRES — GameLogic");
        System.out.println("========================================\n");

        testFeedbackToutCorrect();
        testFeedbackRienCorrect();
        testFeedbackCouleursMaisMailPlacees();
        testFeedbackMixte();
        testFeedbackAvecDoublons();
        testVictoire();
        testGameOver();
        testCouleurInvalide();
        testCombinaisonInvalide();
        testReset();

        System.out.println("\n========================================");
        System.out.println("  RÉSULTATS : " + passed + " passés / " + (passed + failed) + " total");
        if (failed == 0) System.out.println("  ✅ TOUS LES TESTS PASSENT !");
        else             System.out.println("  ❌ " + failed + " test(s) échoué(s)");
        System.out.println("========================================");
    }

    // -----------------------------------------------------------------------
    // Cas de tests
    // -----------------------------------------------------------------------

    static void testFeedbackToutCorrect() {
        GameLogic g = new GameLogic(10);
        g.setSecret(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        int[] fb = g.evaluateGuess(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        // 4 couleurs correctes, 4 positions correctes
        assertTest("Tout correct : couleurs=4", fb[0] == 4);
        assertTest("Tout correct : positions=4", fb[1] == 4);
    }

    static void testFeedbackRienCorrect() {
        GameLogic g = new GameLogic(10);
        g.setSecret(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        int[] fb = g.evaluateGuess(new String[]{"ORANGE", "ORANGE", "ORANGE", "ORANGE"});
        assertTest("Rien correct : couleurs=0", fb[0] == 0);
        assertTest("Rien correct : positions=0", fb[1] == 0);
    }

    static void testFeedbackCouleursMaisMailPlacees() {
        GameLogic g = new GameLogic(10);
        g.setSecret(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        // Toutes les couleurs présentes, mais aucune bien placée
        int[] fb = g.evaluateGuess(new String[]{"GREEN", "BLUE", "YELLOW", "RED"});
        assertTest("Mal placées : couleurs=4", fb[0] == 4);
        assertTest("Mal placées : positions=0", fb[1] == 0);
    }

    static void testFeedbackMixte() {
        GameLogic g = new GameLogic(10);
        g.setSecret(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        // RED bien placé, GREEN présent mais mal placé, ORANGE absent, YELLOW bien placé
        int[] fb = g.evaluateGuess(new String[]{"RED", "ORANGE", "GREEN", "YELLOW"});
        assertTest("Mixte : couleurs=3",   fb[0] == 3); // RED + GREEN + YELLOW
        assertTest("Mixte : positions=2",  fb[1] == 2); // RED + YELLOW
    }

    static void testFeedbackAvecDoublons() {
        GameLogic g = new GameLogic(10);
        // Secret avec doublons
        g.setSecret(new String[]{"RED", "RED", "BLUE", "BLUE"});
        // Proposition avec un seul RED et un seul BLUE
        int[] fb = g.evaluateGuess(new String[]{"RED", "GREEN", "BLUE", "GREEN"});
        assertTest("Doublons : couleurs=2", fb[0] == 2); // 1 RED + 1 BLUE (bien placés)
        assertTest("Doublons : positions=2", fb[1] == 2);
    }

    static void testVictoire() {
        GameLogic g = new GameLogic(10);
        g.setSecret(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        int[] fb = g.evaluateGuess(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        assertTest("Victoire détectée", g.isWin(fb));
    }

    static void testGameOver() {
        GameLogic g = new GameLogic(3);
        g.setSecret(new String[]{"RED", "RED", "RED", "RED"});
        g.evaluateGuess(new String[]{"BLUE", "BLUE", "BLUE", "BLUE"});
        g.evaluateGuess(new String[]{"BLUE", "BLUE", "BLUE", "BLUE"});
        assertTest("Pas encore game over après 2", !g.isGameOver());
        g.evaluateGuess(new String[]{"BLUE", "BLUE", "BLUE", "BLUE"});
        assertTest("Game over après 3 tentatives", g.isGameOver());
    }

    static void testCouleurInvalide() {
        assertTest("PURPLE invalide",    !GameLogic.isValidColor("PURPLE"));
        assertTest("RED valide",          GameLogic.isValidColor("RED"));
        assertTest("red valide (casse)", GameLogic.isValidColor("red"));
        assertTest("ORANGE valide",      GameLogic.isValidColor("ORANGE"));
    }

    static void testCombinaisonInvalide() {
        assertTest("Trop court invalide",
                !GameLogic.isValidCombination(new String[]{"RED", "GREEN", "BLUE"}));
        assertTest("Couleur invalide dans combi",
                !GameLogic.isValidCombination(new String[]{"RED", "GREEN", "BLUE", "PURPLE"}));
        assertTest("null invalide",
                !GameLogic.isValidCombination(null));
        assertTest("Combi valide",
                GameLogic.isValidCombination(new String[]{"RED", "GREEN", "BLUE", "YELLOW"}));
    }

    static void testReset() {
        GameLogic g = new GameLogic(3);
        g.setSecret(new String[]{"RED", "RED", "RED", "RED"});
        g.evaluateGuess(new String[]{"BLUE", "BLUE", "BLUE", "BLUE"});
        g.evaluateGuess(new String[]{"BLUE", "BLUE", "BLUE", "BLUE"});
        assertTest("2 tentatives avant reset", g.getCurrentAttempts() == 2);
        g.reset();
        assertTest("0 tentative après reset", g.getCurrentAttempts() == 0);
        assertTest("Secret null après reset",  g.getSecretCombination() == null);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    static void assertTest(String name, boolean condition) {
        if (condition) {
            System.out.println("  ✅ " + name);
            passed++;
        } else {
            System.out.println("  ❌ ÉCHEC : " + name);
            failed++;
        }
    }
}
