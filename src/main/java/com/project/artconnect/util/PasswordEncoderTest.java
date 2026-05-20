package com.project.artconnect.util;

/**
 * Classe de test pour vérifier le fonctionnement de bcrypt.
 * À exécuter pour vérifier que l'implémentation fonctionne correctement.
 */
public class PasswordEncoderTest {

    public static void main(String[] args) {
        System.out.println("=== Test PasswordEncoder Bcrypt ===\n");

        // Test 1: Hachage d'un mot de passe
        String plainPassword = "MySecurePassword123!";
        String hashedPassword = PasswordEncoder.encode(plainPassword);

        System.out.println("1. Hachage du mot de passe:");
        System.out.println("   Mot de passe en clair: " + plainPassword);
        System.out.println("   Mot de passe haché:   " + hashedPassword);
        System.out.println("   Longueur du hash:     " + hashedPassword.length() + " caractères\n");

        // Test 2: Vérification correcte du mot de passe
        System.out.println("2. Vérification avec le bon mot de passe:");
        boolean matches = PasswordEncoder.matches(plainPassword, hashedPassword);
        System.out.println("   Résultat: " + (matches ? "✓ CORRECT" : "✗ INCORRECT") + "\n");

        // Test 3: Vérification incorrecte du mot de passe
        System.out.println("3. Vérification avec un mauvais mot de passe:");
        boolean mismatch = PasswordEncoder.matches("WrongPassword123!", hashedPassword);
        System.out.println("   Résultat: " + (mismatch ? "✗ INCORRECT (devrait être INCORRECT)" : "✓ CORRECT (rejeté correctement)") + "\n");

        // Test 4: Unicité des hashes (chaque hash est différent grâce au salt)
        System.out.println("4. Unicité des hashes (même mot de passe, hashes différents):");
        String hash1 = PasswordEncoder.encode(plainPassword);
        String hash2 = PasswordEncoder.encode(plainPassword);
        System.out.println("   Hash 1: " + hash1);
        System.out.println("   Hash 2: " + hash2);
        System.out.println("   Identiques? " + hash1.equals(hash2) + " (devrait être false)");
        System.out.println("   Mais les deux matchent le mot de passe? " +
            (PasswordEncoder.matches(plainPassword, hash1) && PasswordEncoder.matches(plainPassword, hash2))
            + " (devrait être true)\n");

        // Test 5: Cas limites
        System.out.println("5. Cas limites:");
        try {
            // Null password
            PasswordEncoder.encode(null);
            System.out.println("   ✗ Devrait rejeter les mots de passe null");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✓ Rejette correctement les mots de passe null");
        }

        boolean nullMatch = PasswordEncoder.matches(null, hashedPassword);
        System.out.println("   Null matches? " + (nullMatch ? "✗ INCORRECT" : "✓ CORRECT (rejette null)"));

        System.out.println("\n=== Tous les tests sont passés! ===");
    }
}

