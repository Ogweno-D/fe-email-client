import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;

public class CredentialSecurityUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    public static String encrypt(String data, String secretKey) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(data.getBytes());
            byte[] encryptedIvAndText = new byte[IV_LENGTH_BYTE + encrypted.length];
            System.arraycopy(iv, 0, encryptedIvAndText, 0, IV_LENGTH_BYTE);
            System.arraycopy(encrypted, 0, encryptedIvAndText, IV_LENGTH_BYTE, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedIvAndText);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String encryptedData, String secretKey) {
        try {
            byte[] encryptedIvAndText = Base64.getDecoder().decode(encryptedData);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(encryptedIvAndText, 0, iv, 0, IV_LENGTH_BYTE);

            int encryptedSize = encryptedIvAndText.length - IV_LENGTH_BYTE;
            byte[] encryptedBytes = new byte[encryptedSize];
            System.arraycopy(encryptedIvAndText, IV_LENGTH_BYTE, encryptedBytes, 0, encryptedSize);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error: " + e.getMessage(), e);
        }
    }
}
