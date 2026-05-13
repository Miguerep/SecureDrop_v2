package util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class CryptoUtil {

    // =========================================================
    // 1. MÉTODOS RSA (Cifrado Asimétrico para intercambiar claves)
    // =========================================================

    public static byte[] encryptRSA(PublicKey publicKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] decryptRSA(PrivateKey privateKey, byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    // =========================================================
    // 2. MÉTODOS AES-GCM (Cifrado Simétrico para los mensajes)
    // =========================================================

    private static final int GCM_IV_LENGTH = 12; // Recomendado para GCM
    private static final int GCM_TAG_LENGTH = 128;

    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // Máxima seguridad AES-256
        return keyGen.generateKey();
    }

    public static byte[] encryptAES_GCM(SecretKey key, byte[] data) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv); // IV siempre aleatorio

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] cipherText = cipher.doFinal(data);

        // Guardamos: [IV (12 bytes)] + [Texto Cifrado]
        byte[] encryptedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

        return encryptedData;
    }

    public static byte[] decryptAES_GCM(SecretKey key, byte[] encryptedData) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length); // Extraemos el IV del principio

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        // Desciframos saltándonos los primeros 12 bytes del IV
        return cipher.doFinal(encryptedData, iv.length, encryptedData.length - iv.length);
    }
}