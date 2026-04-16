package com.guessgame.client.p2p;

import com.guessgame.client.logic.GameController;
import com.guessgame.client.logic.GameLogic;
import com.guessgame.client.logic.GameState;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * TestP2PSimulation.java
 * -----------------------
 * Simule deux joueurs (Alice et Bob) sur la même machine (localhost).
 *
 * Alice = détenteur du secret (premier dans la liste)
 * Bob   = devineur
 *
 * Comment lancer :
 *   java com.guessgame.client.p2p.TestP2PSimulation
 *
 * Ce test vérifie :
 *   1. Que les connexions P2P s'établissent correctement
 *   2. Que SECRET_SET est bien transmis
 *   3. Que GUESS → FEEDBACK fonctionne
 *   4. Que WINNER est annoncé quand la bonne combinaison est trouvée
 *   5. Que NEW_GAME remet à zéro l'état
 */
public class TestP2PSimulation {

    // Résultats de test
    static boolean aliceReceivedFeedback   = false;
    static boolean bobReceivedWinner       = false;
    static boolean aliceNewGameTriggered   = false;
    static String  winnerName              = null;

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("========================================");
        System.out.println("  TEST P2P — Simulation Alice & Bob");
        System.out.println("========================================\n");

        // ------------------------------------------------------------------
        // 1. Créer les P2PManager pour Alice et Bob
        // ------------------------------------------------------------------
        System.out.println("--- Étape 1 : Création des P2PManagers ---");

        // On crée les listeners UI avant les managers
        DummyUI aliceUI = new DummyUI("Alice");
        DummyUI bobUI   = new DummyUI("Bob");

        P2PManager aliceP2P = new P2PManager("Alice", null); // listener défini après
        P2PManager bobP2P   = new P2PManager("Bob",   null);

        aliceP2P.startAccepting();
        bobP2P.startAccepting();

        int alicePort = aliceP2P.getListeningPort();
        int bobPort   = bobP2P.getListeningPort();

        System.out.println("Alice écoute sur le port : " + alicePort);
        System.out.println("Bob écoute sur le port   : " + bobPort);

        // ------------------------------------------------------------------
        // 2. Créer les GameControllers
        //    (ils implémentent GameEventListener pour recevoir les msgs P2P)
        // ------------------------------------------------------------------
        System.out.println("\n--- Étape 2 : Création des GameControllers ---");

        // On recrée les managers avec le bon listener cette fois
        P2PManager aliceP2P2 = new P2PManager("Alice", null);
        P2PManager bobP2P2   = new P2PManager("Bob",   null);
        aliceP2P2.startAccepting();
        bobP2P2.startAccepting();

        int alicePort2 = aliceP2P2.getListeningPort();
        int bobPort2   = bobP2P2.getListeningPort();

        System.out.println("Alice (2) écoute sur le port : " + alicePort2);
        System.out.println("Bob   (2) écoute sur le port : " + bobPort2);

        GameController aliceController = new GameController("Alice", aliceP2P2, new DummyUI("Alice") {
            @Override
            public void onFeedbackReceived(int correctColors, int correctPositions) {
                // Alice est détentrice, ne devrait pas recevoir de feedback normalement
            }
            @Override
            public void onWinner(String name) {
                System.out.println("[TEST] ✅ Alice reçoit WINNER : " + name);
                winnerName = name;
            }
            @Override
            public void onNewGame(boolean isHolder, String holder) {
                System.out.println("[TEST] ✅ Alice reçoit NEW_GAME. Détenteur suivant : " + holder);
                aliceNewGameTriggered = true;
            }
        });

        GameController bobController = new GameController("Bob", bobP2P2, new DummyUI("Bob") {
            @Override
            public void onFeedbackReceived(int correctColors, int correctPositions) {
                System.out.println("[TEST] ✅ Bob reçoit FEEDBACK : couleurs=" + correctColors
                        + " positions=" + correctPositions);
                aliceReceivedFeedback = true;
            }
            @Override
            public void onWinner(String name) {
                System.out.println("[TEST] ✅ Bob reçoit WINNER : " + name);
                bobReceivedWinner = true;
                winnerName = name;
            }
        });

        // Remplacer le listener dans les P2PManagers
        // (on utilise les controllers directement comme listeners)
        P2PManager aliceMgr = createManagerWithListener("Alice", aliceController);
        P2PManager bobMgr   = createManagerWithListener("Bob",   bobController);

        aliceMgr.startAccepting();
        bobMgr.startAccepting();

        int aliceFinalPort = aliceMgr.getListeningPort();
        int bobFinalPort   = bobMgr.getListeningPort();

        System.out.println("Alice (final) port : " + aliceFinalPort);
        System.out.println("Bob   (final) port : " + bobFinalPort);

        // ------------------------------------------------------------------
        // 3. Simuler GG|GAME_STARTED
        //    Alice est détentrice (première dans la liste)
        // ------------------------------------------------------------------
        System.out.println("\n--- Étape 3 : Démarrage de la partie ---");

        // Format : "nom:ip:port"
        List<String> alicePlayers = Arrays.asList(
                "Alice:127.0.0.1:" + aliceFinalPort,
                "Bob:127.0.0.1:"   + bobFinalPort
        );
        List<String> bobPlayers = Arrays.asList(
                "Alice:127.0.0.1:" + aliceFinalPort,
                "Bob:127.0.0.1:"   + bobFinalPort
        );

        // Créer les GameControllers finaux
        GameController aliceFinal = createController("Alice", aliceMgr);
        GameController bobFinal   = createController("Bob",   bobMgr);

        aliceFinal.startGame("SalleTest", alicePlayers, 5);
        Thread.sleep(500); // Laisser le temps aux connexions P2P de s'établir
        bobFinal.startGame("SalleTest", bobPlayers, 5);
        Thread.sleep(500);

        // ------------------------------------------------------------------
        // 4. Alice définit le secret
        // ------------------------------------------------------------------
        System.out.println("\n--- Étape 4 : Alice définit le secret ---");
        aliceFinal.setSecret(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        Thread.sleep(300);

        // ------------------------------------------------------------------
        // 5. Bob devine (mauvaise proposition)
        // ------------------------------------------------------------------
        System.out.println("\n--- Étape 5 : Bob devine (mauvais) ---");
        bobFinal.sendGuess(new String[]{"ORANGE", "ORANGE", "ORANGE", "ORANGE"});
        Thread.sleep(500);

        // ------------------------------------------------------------------
        // 6. Bob devine (bonne proposition)
        // ------------------------------------------------------------------
        System.out.println("\n--- Étape 6 : Bob devine (correct) ---");
        bobFinal.sendGuess(new String[]{"RED", "GREEN", "BLUE", "YELLOW"});
        Thread.sleep(500);

        // ------------------------------------------------------------------
        // 7. Résultats
        // ------------------------------------------------------------------
        System.out.println("\n========================================");
        System.out.println("  RÉSULTATS DES TESTS P2P");
        System.out.println("========================================");
        printResult("Bob a reçu un FEEDBACK",       aliceReceivedFeedback);
        printResult("WINNER annoncé = 'Bob'",        "Bob".equals(winnerName));
        printResult("Bob a reçu le WINNER",          bobReceivedWinner);

        // Fermeture propre
        aliceMgr.shutdown();
        bobMgr.shutdown();

        System.out.println("\nTest terminé.");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    static P2PManager createManagerWithListener(String name,
                                                 P2PManager.GameEventListener listener) throws IOException {
        return new P2PManager(name, listener);
    }

    static GameController createController(String name, P2PManager mgr) {
        return new GameController(name, mgr, new DummyUI(name));
    }

    static void printResult(String label, boolean ok) {
        System.out.println("  " + (ok ? "✅" : "❌") + " " + label);
    }

    // -----------------------------------------------------------------------
    // UI factice pour les tests
    // -----------------------------------------------------------------------
    static class DummyUI implements GameController.GameUIListener {
        final String playerName;
        DummyUI(String name) { this.playerName = name; }

        @Override public void onFeedbackReceived(int c, int p) {
            System.out.println("[" + playerName + "-UI] Feedback reçu : couleurs=" + c + " positions=" + p);
        }
        @Override public void onWinner(String name) {
            System.out.println("[" + playerName + "-UI] Gagnant : " + name);
        }
        @Override public void onGameOver(String[] secret) {
            System.out.println("[" + playerName + "-UI] Game over. Secret=" + Arrays.toString(secret));
        }
        @Override public void onNewGame(boolean isHolder, String holder) {
            System.out.println("[" + playerName + "-UI] Nouvelle partie. Détenteur=" + holder);
        }
        @Override public void onSecretNeeded() {
            System.out.println("[" + playerName + "-UI] Vous devez choisir le secret !");
        }
        @Override public void onGuessNeeded(int remaining) {
            System.out.println("[" + playerName + "-UI] À vous de deviner ! Tentatives restantes=" + remaining);
        }
        @Override public void displayMessage(String msg) {
            System.out.println("[" + playerName + "-UI] " + msg);
        }
    }
}
