package com.fitpay.android.utils;

import com.fitpay.android.api.models.security.ECCKeyPair;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;

import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.interfaces.ECPublicKey;
import java.util.Properties;

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
            SecurityProvider.getInstance().initProvider();
            encrypter.getJCAContext().setProvider(SecurityProvider.getInstance().getProvider());
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
        KeysManager keysManager = KeysManager.getInstance();

        JWEObject jweObject;
        try {
            jweObject = JWEObject.parse(encryptedString);
            JWEHeader jweHeader = jweObject.getHeader();
            if (jweHeader.getKeyID() == null || jweHeader.getKeyID().equals(keysManager.getKeyId(type))) {
                jweObject.decrypt(new AESDecrypter(keysManager.getSecretKey(type)));

                if ("JWT".equals(jweObject.getHeader().getContentType())) {
                    SignedJWT signedJwt = jweObject.getPayload().toSignedJWT();
                    ECCKeyPair keyPair = keysManager.getPairForType(type);

                    ECPublicKey key = null;
                    if ("https://fit-pay.com".equals(signedJwt.getJWTClaimsSet().getIssuer())) {
                        key = (ECPublicKey)keysManager.getPublicKey("EC", Hex.hexStringToBytes(keyPair.getServerPublicKey()));
                    } else {
                        key = (ECPublicKey)keysManager.getPublicKey("EC", Hex.hexStringToBytes(keyPair.getPublicKey()));
                    }
                    JWSVerifier verifier = new ECDSAVerifier(key);
                    if (!signedJwt.verify(verifier)) {
                        throw new IllegalArgumentException("jwt did not pass signature validation");
                    }

                    return signedJwt.getJWTClaimsSet().getStringClaim("data");
                } else {
                    return jweObject.getPayload().toString();
                }
            }
        } catch (Exception e) {
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

    /**
     * Convert config properties string into Properties
     * @param input config string
     * @return properties
     * @throws IOException
     */
    public static Properties convertCommaSeparatedList(String input) throws IOException {
        if (null == input) {
            return null;
        }
        String propertiesFormat = input.replaceAll(",", "\n");
        Properties properties = new Properties();
        properties.load(new StringReader(propertiesFormat));
        return properties;
    }
}
