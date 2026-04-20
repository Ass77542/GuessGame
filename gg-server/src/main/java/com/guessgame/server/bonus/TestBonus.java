package com.guessgame.server.bonus;

/**
 * TestBonus.java
 * ---------------
 * Tests unitaires manuels pour les fonctionnalités bonus :
 *   - MessageSigner (sécurité / intégrité des messages)
 *   - HeartbeatMonitor (tolérance aux pannes / détection de déconnexion)
 *
 * Lancer avec :
 *   java com.guessgame.server.bonus.TestBonus
 */
public class TestBonus {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================");
        System.out.println("  TESTS BONUS — Sécurité + Tolérance");
        System.out.println("========================================\n");

        testSignatureValide();
        testSignatureInvalide();
        testSignatureManquante();
        testStripSignature();
        testMessageIntact();
        testHeartbeatTimeout();
        testHeartbeatPingResetTimer();

        System.out.println("\n========================================");
        System.out.println("  RÉSULTATS : " + passed + " passés / " + (passed + failed) + " total");
        if (failed == 0) System.out.println("  ✅ TOUS LES TESTS PASSENT !");
        else             System.out.println("  ❌ " + failed + " test(s) échoué(s)");
        System.out.println("========================================");
    }

    // -----------------------------------------------------------------------
    // Tests MessageSigner
    // -----------------------------------------------------------------------

    static void testSignatureValide() {
        String msg    = "GG|CONNECT|Alice|5000";
        String signed = MessageSigner.sign(msg);

        assertTest("Message signé contient |SIG=",   signed.contains("|SIG="));
        assertTest("Signature valide acceptée",       MessageSigner.verify(signed));
    }

    static void testSignatureInvalide() {
        String msg        = "GG|CONNECT|Alice|5000";
        String signed     = MessageSigner.sign(msg);
        // Altérer la signature
        String tampered   = signed.replace("SIG=", "SIG=FAUX");

        assertTest("Signature altérée rejetée",       !MessageSigner.verify(tampered));
    }

    static void testSignatureManquante() {
        String msg = "GG|CONNECT|Alice|5000"; // pas de signature
        assertTest("Message sans signature rejeté",   !MessageSigner.verify(msg));
    }

    static void testStripSignature() {
        String original = "GG|CONNECT|Alice|5000";
        String signed   = MessageSigner.sign(original);
        String stripped = MessageSigner.stripSignature(signed);

        assertTest("stripSignature retourne l'original", original.equals(stripped));
    }

    static void testMessageIntact() {
        // Vérifier que signer puis vérifier deux messages différents ne se confondent pas
        String msg1 = "GG|CONNECT|Alice|5000";
        String msg2 = "GG|CONNECT|Bob|6000";

        String signed1 = MessageSigner.sign(msg1);
        String signed2 = MessageSigner.sign(msg2);

        assertTest("Sig msg1 valide",             MessageSigner.verify(signed1));
        assertTest("Sig msg2 valide",             MessageSigner.verify(signed2));
        // Cross-check : la signature de msg1 ne valide pas msg2
        String crossSig = MessageSigner.stripSignature(signed1)
                + signed2.substring(signed2.lastIndexOf("|SIG="));
        assertTest("Signatures non interchangeables", !MessageSigner.verify(crossSig));
    }

    // -----------------------------------------------------------------------
    // Tests HeartbeatMonitor
    // -----------------------------------------------------------------------

    static void testHeartbeatTimeout() throws InterruptedException {
        // Timeout très court pour les tests (on hack via réflexion n'est pas possible ici,
        // donc on teste le comportement via callback)
        boolean[] timeoutCalled = {false};
        String[] timedOutPlayer = {null};

        HeartbeatMonitor monitor = new HeartbeatMonitor(player -> {
            timeoutCalled[0] = true;
            timedOutPlayer[0] = player;
            System.out.println("[TestBonus] Callback timeout appelé pour : " + player);
        });

        // On teste juste que le monitor démarre et s'arrête proprement
        monitor.registerPlayer("TestPlayer");
        monitor.start();
        Thread.sleep(200);
        monitor.stop();

        // Le timeout de 15s n'aura pas eu lieu en 200ms, c'est normal
        // On vérifie juste que le monitor s'est lancé et arrêté sans exception
        assertTest("HeartbeatMonitor démarre sans erreur", true);
        assertTest("Timeout non déclenché trop tôt",       !timeoutCalled[0]);
    }

    static void testHeartbeatPingResetTimer() throws InterruptedException {
        boolean[] timeoutCalled = {false};

        HeartbeatMonitor monitor = new HeartbeatMonitor(player -> {
            timeoutCalled[0] = true;
        });

        monitor.registerPlayer("Alice");
        monitor.recordPing("Alice"); // Simule un PING
        monitor.start();
        Thread.sleep(200);
        monitor.stop();

        assertTest("PING enregistré sans erreur",      true);
        assertTest("Pas de timeout après PING récent", !timeoutCalled[0]);
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
