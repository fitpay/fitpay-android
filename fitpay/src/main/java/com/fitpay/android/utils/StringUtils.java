package com.fitpay.android.utils;

import com.fitpay.android.BuildConfig;
import com.fitpay.android.api.models.security.ECCKeyPair;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.util.Base64URL;

import java.security.MessageDigest;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by Vlad on 26.02.2016.
 */
public final class StringUtils {

    /**
     * Get encrypted string
     *
     * @param type            key type
     * @param decryptedString decrypted string
     * @return encrypted string
     */
    public static String getEncryptedString(@KeysManager.KeyType int type, String decryptedString) {

        JWEAlgorithm alg = JWEAlgorithm.A256GCMKW;
        EncryptionMethod enc = EncryptionMethod.A256GCM;

        ECCKeyPair keyPair = KeysManager.getInstance().getPairForType(type);

        if (null == keyPair) {
            throw new IllegalStateException("No key pair available for type (type = " + type + ")");
        }

        JWEHeader.Builder jweHeaderBuilder = new JWEHeader.Builder(alg, enc)
                .contentType("application/json")
                .keyID(keyPair.getKeyId());

        JWEHeader header = jweHeaderBuilder.build();
        Payload payload = new Payload(decryptedString);
        JWEObject jweObject = new JWEObject(header, payload);
        try {
            JWEEncrypter encrypter = new AESEncrypter(KeysManager.getInstance().getSecretKey(type));
            jweObject.encrypt(encrypter);
        } catch (JOSEException e) {
            FPLog.e(e);
        }

        return jweObject.serialize();
    }

    /**
     * Get decrypted string
     *
     * @param type            key type
     * @param encryptedString encrypted string
     * @return decrypted string
     */
    public static String getDecryptedString(@KeysManager.KeyType int type, String encryptedString) {

        JWEObject jweObject;
        try {
            jweObject = JWEObject.parse(encryptedString);
            JWEHeader jweHeader = jweObject.getHeader();
            if (jweHeader.getKeyID() == null || jweHeader.getKeyID().equals(KeysManager.getInstance().getKeyId(type))) {
                jweObject.decrypt(new AESDecrypter(KeysManager.getInstance().getSecretKey(type)));
                return jweObject.getPayload().toString();
            }
        } catch (ParseException | JOSEException e) {
            FPLog.e(e);
        }

        return null;
    }

    /**
     * Convert String to SHA1
     *
     * @param inputString original string
     * @return converted string
     */
    public static String toSHA1(String inputString) {
        StringBuilder sb = new StringBuilder();

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] result = digest.digest(inputString.getBytes("UTF-8"));
            for (byte b : result) // This is your byte[] result..
            {
                sb.append(String.format("%02X", b));
            }
        } catch (Exception e) {
            FPLog.e(e);
        }

        return sb.toString().toLowerCase();
    }

    public static String base64UrlEncode(String inputString) {
        return Base64URL.encode(inputString).toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String convertHexStringToAscii(String hex) {
        if (null == hex) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
}
