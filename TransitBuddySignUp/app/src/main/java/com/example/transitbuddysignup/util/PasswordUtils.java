package com.example.transitbuddysignup.util;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 */
public class PasswordUtils {
    private static final String TAG = "PasswordUtils";
    private static final String HASH_ALGORITHM = "SHA-512";
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;

    /**
     * Generate a random salt for password hashing
     * @return Base64 encoded salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash a password with the given salt
     * @param password The password to hash
     * @param salt The salt for hashing
     * @return Hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            
            // Create hash
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.reset();
            md.update(saltBytes);
            
            byte[] input = password.getBytes(StandardCharsets.UTF_8);
            byte[] hashed = md.digest(input);
            
            // Apply multiple iterations
            for (int i = 0; i < ITERATIONS; i++) {
                md.reset();
                hashed = md.digest(hashed);
            }
            
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verify a password against a stored hash and salt
     * @param inputPassword The password to verify
     * @param storedHash The stored hash
     * @param storedSalt The stored salt
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) {
        String calculatedHash = hashPassword(inputPassword, storedSalt);
        return calculatedHash.equals(storedHash);
    }
} 