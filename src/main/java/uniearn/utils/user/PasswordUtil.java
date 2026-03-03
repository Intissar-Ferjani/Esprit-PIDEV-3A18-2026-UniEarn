package uniearn.utils.user;

import org.mindrot.jbcrypt.BCrypt;


public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Generate a salt and hash the password in one step
        // Salt = a random secret string added to the password before hashing.
        // The cost factor (10) determines how computationally expensive the hashing is
        // Higher = more secure but slower. 10-12 is recommended.
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));

        System.out.println("✓ Password hashed successfully");
        return hashedPassword;
    }

    //verify if pass is hashed
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
            System.out.println("⚠ Password verification failed: invalid hash format");
            return false;
        }
    }

    //check if it's hashed
    public static boolean isHashed(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        return password.matches("^\\$2[ayb]\\$.{56}$");
    }


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
     * Main method for testing
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