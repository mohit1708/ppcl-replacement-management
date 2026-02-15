package com.ppcl.replacement.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES encryption utility for passwords, OTPs and courier IDs.
 * Uses AES/ECB/PKCS5Padding with a SHA-256 hashed 128-bit key.
 */
public class EncryptionUtil {

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
	private static final String SECRET_KEY = "mY#S3cUr3!KeY@2025$EnCrYpT10n";

	/** Derives a 128-bit AES key from the secret key string using SHA-256. */
	private static SecretKeySpec getSecretKey() throws Exception {
		byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16);
		return new SecretKeySpec(key, ALGORITHM);
	}

	/** Encrypts the given plain text and returns a URL-safe Base64 string. */
	public static String encrypt(String input) {
		if (input == null || input.isEmpty()) {
			throw new RuntimeException("Cannot encrypt null or empty input");
		}
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
			byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error encrypting data: " + e.getMessage(), e);
		}
	}

	/** Decrypts a URL-safe Base64 encrypted string back to plain text. */
	public static String decrypt(String encryptedInput) {
		if (encryptedInput == null || encryptedInput.isEmpty()) {
			throw new RuntimeException("Cannot decrypt null or empty input");
		}
		try {
			String trimmed = encryptedInput.trim();
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
			byte[] decodedBytes = Base64.getUrlDecoder().decode(trimmed);
			byte[] decryptedBytes = cipher.doFinal(decodedBytes);
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("DECRYPT ERROR - Input: [" + encryptedInput + "] Length: " + encryptedInput.length());
			throw new RuntimeException("Error decrypting data: " + e.getMessage(), e);
		}
	}

	/**
	 * Compares a plain text value against an encrypted value without decrypting.
	 * Works because AES ECB is deterministic (same input always produces same output).
	 */
	public static boolean matches(String plainText, String encryptedValue) {
		if (plainText == null || encryptedValue == null) {
			return false;
		}
		try {
			String encrypted = encrypt(plainText);
			return encrypted.equals(encryptedValue.trim());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
