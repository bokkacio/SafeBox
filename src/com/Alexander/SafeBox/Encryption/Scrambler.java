package com.Alexander.SafeBox.Encryption;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.util.Base64;

/*
Encrypt and decrypt String value using PKCS12 derivation algorithm
*/
public class Scrambler {

    private String _keyPhrase = null;

    private static final String PKCS12_DERIVATION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";
    private static final int ITERATION_COUNT = 500;
    private static final int KEY_LENGTH = 256;
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String DELIMITER = "]";
    private static final int PKCS5_SALT_LENGTH = 8;

    private static SecureRandom random = new SecureRandom();

    public Scrambler(String keyPhrase)
    {
        _keyPhrase = keyPhrase;
    }

    public String Encrypt(String secretPhrase)
    {
        byte[] salt = GenerateSalt();
        SecretKey key = DeriveKey(salt, _keyPhrase);
        String encryptedSecretPhrase = ProcessEncrypt(secretPhrase, key, salt);
        return encryptedSecretPhrase;
    }

    public String Decrypt(String encryptedPhrase) {
        String[] fields = encryptedPhrase.split(DELIMITER);
        if (fields.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted text format");
        }

        byte[] salt = FromBase64(fields[0]);
        byte[] cipherBytes = FromBase64(fields[1]);
        SecretKey key = DeriveKey(salt, _keyPhrase);
        return ProcessDecrypt(cipherBytes, key, salt);
    }

    private static SecretKey DeriveKey(byte[] salt, String keyPhrase) {
        try {
            KeySpec keySpec = new PBEKeySpec(keyPhrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PKCS12_DERIVATION_ALGORITHM);
            SecretKey result = keyFactory.generateSecret(keySpec);
            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static String ProcessEncrypt(String plaintext, SecretKey key, byte[] salt) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            PBEParameterSpec pbeSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeSpec);
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));
            return String.format("%s%s%s", ToBase64(salt), DELIMITER, ToBase64(cipherText));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String ProcessDecrypt(byte[] cipherBytes, SecretKey key, byte[] salt) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            PBEParameterSpec pbeSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
            cipher.init(Cipher.DECRYPT_MODE, key, pbeSpec);
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            String decryptedPhrase = new String(plainBytes, "UTF-8");
            return decryptedPhrase;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String ToBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static byte[] FromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

    private static byte[] GenerateSalt() {
        byte[] bArray = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(bArray);
        return bArray;
    }
}

