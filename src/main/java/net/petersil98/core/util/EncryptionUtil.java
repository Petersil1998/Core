package net.petersil98.core.util;

import net.petersil98.core.Core;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import net.petersil98.core.util.settings.Settings;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class, which provides Methods to {@link #encrypt(String) encrypt} and {@link #decrypt(String) decrypt}
 * a String. Can be used as a default implementation for encrypting and decrypting the API Key.
 * @see Settings#setDecryptor(Function)
 * @see Settings#setAPIKey(Supplier)
 */
public class EncryptionUtil {

    private static final String SECRET_KEY_1 = "u8x/A?D(G+KbPeSg";
    private static final String SECRET_KEY_2 = "WnZr4u7x!A%D*G-K";

    private static IvParameterSpec ivParameterSpec;
    private static SecretKeySpec secretKeySpec;
    private static Cipher cipher;

    private static final Marker MARKER = MarkerManager.getMarker(EncryptionUtil.class.getSimpleName());

    static {
        try {
            ivParameterSpec = new IvParameterSpec(SECRET_KEY_1.getBytes(StandardCharsets.UTF_8));
            secretKeySpec = new SecretKeySpec(SECRET_KEY_2.getBytes(StandardCharsets.UTF_8), "AES");
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to <b>encrypt</b> a String. This is just a default Implementation, feel free to use a different, more secure one.
     * @param string The String to be encrypted
     * @return The encrypted String
     */
    public static String encrypt(String string) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(string.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            Core.LOGGER.error(MARKER, "Failed to encode data", e);
        }
        return "";
    }

    /**
     * Method to <b>decrypt</b> a String. This is just a default Implementation, feel free to use a different, more secure one.
     * @param string The encrypted String, that should get decrypted
     * @return The decrypted String
     */
    public static String decrypt(String string) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(string));
            return new String(decryptedBytes);
        } catch (Exception e) {
            Core.LOGGER.error(MARKER, "Failed to decode data", e);
        }
        return "";
    }
}
