package com.guessgame.server.bonus;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * MessageSigner.java
 * -------------------
 * Ajoute une signature HMAC-SHA256 à chaque message GG pour garantir
 * l'intégrité et l'authenticité des messages échangés entre client et serveur.
 *
 * Format du message signé :
 *   GG|COMMANDE|args...|SIG=<signature_base64>
 *
 * Mécanisme :
 *   1. Le serveur et le client partagent une clé secrète (SECRET_KEY).
 *   2. Avant d'envoyer un message, on calcule SHA-256(message + SECRET_KEY).
 *   3. À la réception, on vérifie que la signature correspond.
 *   4. Si la signature est invalide → le message est rejeté.
 *
 * HYPOTHÈSE :
 *   - La clé partagée est fixe dans le code (simplifié pour ce projet).
 *   - Dans un vrai système, elle serait échangée via un handshake sécurisé (TLS).
 */
public class MessageSigner {

    // Clé partagée entre client et serveur
    // HYPOTHÈSE : clé fixe pour ce projet (simplification)
    private static final String SECRET_KEY = "GG_SECRET_6GEN723_UQAC";

    /**
     * Signe un message GG en ajoutant une signature à la fin.
     *
     * @param message Message GG original (ex: "GG|CONNECT|Alice|5000")
     * @return Message signé (ex: "GG|CONNECT|Alice|5000|SIG=abc123...")
     */
    public static String sign(String message) {
        String signature = computeSignature(message);
        return message + "|SIG=" + signature;
    }

    /**
     * Vérifie qu'un message signé est authentique.
     *
     * @param signedMessage Message avec signature (ex: "GG|CONNECT|Alice|SIG=abc...")
     * @return true si la signature est valide
     */
    public static boolean verify(String signedMessage) {
        int sigIndex = signedMessage.lastIndexOf("|SIG=");
        if (sigIndex == -1) {
            System.err.println("[MessageSigner] Aucune signature trouvée dans : " + signedMessage);
            return false;
        }

        String originalMessage = signedMessage.substring(0, sigIndex);
        String receivedSig     = signedMessage.substring(sigIndex + 5); // après "|SIG="
        String expectedSig     = computeSignature(originalMessage);

        boolean valid = expectedSig.equals(receivedSig);
        if (!valid) {
            System.err.println("[MessageSigner] Signature invalide pour : " + originalMessage);
            System.err.println("  Attendue : " + expectedSig);
            System.err.println("  Reçue    : " + receivedSig);
        }
        return valid;
    }

    /**
     * Extrait le message original d'un message signé (retire la signature).
     *
     * @param signedMessage Message avec signature
     * @return Message original sans la partie "|SIG=..."
     */
    public static String stripSignature(String signedMessage) {
        int sigIndex = signedMessage.lastIndexOf("|SIG=");
        if (sigIndex == -1) return signedMessage;
        return signedMessage.substring(0, sigIndex);
    }

    /**
     * Calcule SHA-256(message + SECRET_KEY) encodé en Base64.
     */
    private static String computeSignature(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String toHash = message + SECRET_KEY;
            byte[] hashBytes = digest.digest(toHash.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("[MessageSigner] Erreur calcul signature : " + e.getMessage(), e);
        }
    }
}
