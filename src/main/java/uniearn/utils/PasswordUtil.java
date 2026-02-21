package uniearn.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt
 *
 * BCrypt is a password hashing function designed to be slow and computationally expensive,
 * making it resistant to brute-force attacks. It automatically handles salting.
 *
 * Usage:
 *   String hashed = PasswordUtil.hashPassword("myPassword123");
 *   boolean isValid = PasswordUtil.verifyPassword("myPassword123", hashed);
 */
public class PasswordUtil {

    /**
     * Hash a plain text password using BCrypt
     *
     * @param plainPassword The plain text password to hash
     * @return The hashed password (includes salt automatically)
     *
     * Example:
     *   String hashed = PasswordUtil.hashPassword("SecurePass123");
     *   // Returns: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Generate a salt and hash the password in one step
        // The cost factor (10) determines how computationally expensive the hashing is
        // Higher = more secure but slower. 10-12 is recommended.
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));

        System.out.println("✓ Password hashed successfully");
        return hashedPassword;
    }

    /**
     * Verify a plain text password against a hashed password
     *
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if the password matches, false otherwise
     *
     * Example:
     *   String stored = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
     *   boolean isValid = PasswordUtil.verifyPassword("SecurePass123", stored);
     *   // Returns: true if password matches
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            System.out.println("⚠ Password verification failed: password is null or empty");
            return false;
        }

        if (hashedPassword == null || hashedPassword.isEmpty()) {
            System.out.println("⚠ Password verification failed: hashed password is null or empty");
            return false;
        }

        try {
            // BCrypt handles the comparison including salt extraction
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);

            if (matches) {
                System.out.println("✓ Password verification successful");
            } else {
                System.out.println("⚠ Password verification failed: password does not match");
            }

            return matches;

        } catch (IllegalArgumentException e) {
            // This can happen if the hashed password is not in valid BCrypt format
            System.out.println("⚠ Password verification failed: invalid hash format");
            return false;
        }
    }

    /**
     * Check if a string is already a BCrypt hash
     *
     * @param password The string to check
     * @return true if it's a BCrypt hash, false otherwise
     *
     * BCrypt hashes start with $2a$, $2b$, or $2y$ and are 60 characters long
     */
    public static boolean isHashed(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // BCrypt hashes start with $2a$, $2b$, or $2y$ and are typically 60 chars
        return password.matches("^\\$2[ayb]\\$.{56}$");
    }

    /**
     * Generate a random secure password (useful for temporary passwords)
     *
     * @param length The desired password length (minimum 8)
     * @return A randomly generated password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();

        java.security.SecureRandom random = new java.security.SecureRandom();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    /**
     * Main method for testing the PasswordUtil class
     */
    public static void main(String[] args) {
        System.out.println("=== BCrypt Password Utility Test ===\n");

        // Test 1: Hash a password
        String plainPassword = "MySecurePassword123";
        System.out.println("Plain password: " + plainPassword);

        String hashed1 = hashPassword(plainPassword);
        System.out.println("Hashed password: " + hashed1);
        System.out.println("Hash length: " + hashed1.length());

        // Test 2: Hash the same password again (should be different due to different salt)
        String hashed2 = hashPassword(plainPassword);
        System.out.println("\nHashed again: " + hashed2);
        System.out.println("Hashes are different: " + !hashed1.equals(hashed2));

        // Test 3: Verify correct password
        System.out.println("\n=== Verification Tests ===");
        boolean result1 = verifyPassword(plainPassword, hashed1);
        System.out.println("Correct password verification: " + result1);

        // Test 4: Verify wrong password
        boolean result2 = verifyPassword("WrongPassword", hashed1);
        System.out.println("Wrong password verification: " + result2);

        // Test 5: Check if string is hashed
        System.out.println("\n=== Hash Detection ===");
        System.out.println("Is 'MyPassword' hashed? " + isHashed("MyPassword"));
        System.out.println("Is hashed password detected? " + isHashed(hashed1));

        // Test 6: Generate random password
        System.out.println("\n=== Random Password ===");
        String randomPwd = generateRandomPassword(12);
        System.out.println("Random password: " + randomPwd);
        System.out.println("Random password hashed: " + hashPassword(randomPwd));
    }
}