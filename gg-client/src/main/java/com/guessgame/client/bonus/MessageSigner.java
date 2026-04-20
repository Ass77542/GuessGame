package com.guessgame.client.bonus;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * MessageSigner.java (client)
 * ----------------------------
 * Identique à la version serveur.
 * Permet au client de signer ses messages et vérifier ceux du serveur.
 *
 * Format du message signé :
 *   GG|COMMANDE|args...|SIG=<signature_base64>
 *
 * HYPOTHÈSE :
 *   - La clé partagée est fixe dans le code (simplification pour ce projet).
 *   - Dans un vrai système, elle serait échangée via TLS.
 */
public class MessageSigner {

    private static final String SECRET_KEY = "GG_SECRET_6GEN723_UQAC";

    public static String sign(String message) {
        String signature = computeSignature(message);
        return message + "|SIG=" + signature;
    }

    public static boolean verify(String signedMessage) {
        int sigIndex = signedMessage.lastIndexOf("|SIG=");
        if (sigIndex == -1) {
            System.err.println("[MessageSigner] Aucune signature trouvée dans : " + signedMessage);
            return false;
        }
        String originalMessage = signedMessage.substring(0, sigIndex);
        String receivedSig     = signedMessage.substring(sigIndex + 5);
        String expectedSig     = computeSignature(originalMessage);

        boolean valid = expectedSig.equals(receivedSig);
        if (!valid) {
            System.err.println("[MessageSigner] Signature invalide pour : " + originalMessage);
        }
        return valid;
    }

    public static String stripSignature(String signedMessage) {
        int sigIndex = signedMessage.lastIndexOf("|SIG=");
        if (sigIndex == -1) return signedMessage;
        return signedMessage.substring(0, sigIndex);
    }

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
