package com.fitpay.android.utils;


import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.security.ECCKeyPair;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.KeyAgreement;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

/**
 * KeysManager is designed to create and manage @ECCKeyPair object.
 */
final public class KeysManager {
    private static final String TAG = KeysManager.class.getName();

    public static final int KEY_API = 0;
    public static final int KEY_WV = KEY_API + 1;
    public static final int KEY_FPCTRL = KEY_WV + 1;

    private static final String ALGORITHM = "EC";
    private static final String KEY_AGREEMENT = "ECDH";
    private static final String EC_CURVE = "secp256r1";
    private static final String KEY_TYPE = "AES";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            KeysManager.KEY_API,
            KeysManager.KEY_WV,
            KeysManager.KEY_FPCTRL
    })
    public @interface KeyType {
    }

    private static KeysManager sInstance;

    public static KeysManager getInstance() {
        if (sInstance == null) {
            sInstance = new KeysManager();
            SecurityProvider.getInstance().initProvider();
        }

        return sInstance;
    }

    public static void clear() {
        sInstance = null;
    }

    private Map<Integer, ECCKeyPair> mKeysMap;

    private KeysManager() {
        mKeysMap = new HashMap<>();
    }

    // Create the public and private keys
    private ECCKeyPair createECCKeyPair() throws Exception {
        KeyPairGenerator keyGenerator;
        if (SecurityProvider.getInstance().getProvider() != null) {
            keyGenerator = KeyPairGenerator.getInstance(ALGORITHM, SecurityProvider.getInstance().getProvider());
        } else {
            keyGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        }
        keyGenerator.initialize(new ECGenParameterSpec(EC_CURVE), new SecureRandom());

        KeyPair keyPair = keyGenerator.generateKeyPair();

        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

        ECCKeyPair eccKeyPair = new ECCKeyPair();
        eccKeyPair.setKeyId(UUID.randomUUID().toString());

        eccKeyPair.setPrivateKey(Hex.bytesToHexString(privateKey.getEncoded()));
        eccKeyPair.setPublicKey(Hex.bytesToHexString(publicKey.getEncoded()));

        return eccKeyPair;
    }

    // methods for ASN.1 encoded keys


    public PrivateKey getPrivateKey(byte[] privateKey) throws Exception {
        KeyFactory kf;
        if (SecurityProvider.getInstance().getProvider() != null) {
            kf = KeyFactory.getInstance(ALGORITHM, SecurityProvider.getInstance().getProvider());
        } else {
            kf = KeyFactory.getInstance(ALGORITHM);
        }
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        return kf.generatePrivate(keySpec);
    }

    public PublicKey getPublicKey(byte[] publicKey) throws Exception {
        return getPublicKey(ALGORITHM, publicKey);
    }

    public PublicKey getPublicKey(String algorithm, byte[] publicKey) throws Exception {
        KeyFactory kf;
        if (SecurityProvider.getInstance().getProvider() != null) {
            kf = KeyFactory.getInstance(algorithm, SecurityProvider.getInstance().getProvider());
        } else {
            kf = KeyFactory.getInstance(algorithm);
        }
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        return kf.generatePublic(keySpec);
    }

    public byte[] getSecretKey(@KeyType int type) {

        ECCKeyPair keyPair = getPairForType(type);
        byte[] secretKey = keyPair.getSecretKey();

        if (secretKey == null) {
            secretKey = createSecretKey(keyPair.getPrivateKey(), keyPair.getServerPublicKey());
            keyPair.setSecretKey(secretKey);
        }

        return secretKey;
    }

    private byte[] createSecretKey(String privateKeyStr, String publicKeyStr) {

        try {
            PrivateKey privateKey = getPrivateKey(Hex.hexStringToBytes(privateKeyStr));
            PublicKey publicKey = getPublicKey(Hex.hexStringToBytes(publicKeyStr));

            KeyAgreement keyAgreement;
            if (SecurityProvider.getInstance().getProvider() != null) {
                keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT, SecurityProvider.getInstance().getProvider());
            } else {
                keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT);
            }

            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            return keyAgreement.generateSecret();
        } catch (Exception e) {
            FPLog.e(TAG, e);
            return null;
        }
    }

    public ECCKeyPair getPairForType(@KeyType int type) {
        return mKeysMap.get(type);
    }

    public ECCKeyPair createPairForType(@KeyType int type) throws Exception {
        removePairForType(type);

        ECCKeyPair keyPair = createECCKeyPair();
        mKeysMap.put(type, keyPair);
        return keyPair;
    }

    public void removePairForType(@KeyType int type) {
        mKeysMap.remove(type);
    }

    @SuppressWarnings("CheckResult")
    public void updateECCKey(final @KeyType int type, @NonNull final Runnable successRunnable, final ApiCallback callback) {
        Single.create((SingleOnSubscribe<ECCKeyPair>) emitter -> {
            try {
                ECCKeyPair keyPair = createPairForType(type);
                Call<ECCKeyPair> getKeyCall = ApiManager.getInstance().getClient().createEncryptionKey(keyPair);
                Response<ECCKeyPair> response = getKeyCall.execute();
                if (response.isSuccessful() && response.errorBody() == null) {
                    emitter.onSuccess(response.body());
                } else if (response.errorBody() != null) {
                    try {
                        emitter.onError(new Throwable(response.errorBody().toString()));
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                } else {
                    emitter.onError(new Throwable(response.message()));
                }

            } catch (Exception e) {
                emitter.onError(e);
            }
        }).doOnError(throwable -> {
            FPLog.e(TAG, throwable);
            callback.onFailure(ResultCode.REQUEST_FAILED, throwable.toString());
        }).doOnSuccess(eccKeyPair -> {
            eccKeyPair.setPrivateKey(mKeysMap.get(type).getPrivateKey());
            mKeysMap.put(type, eccKeyPair);
            successRunnable.run();
        }).subscribeOn(Schedulers.io()).blockingGet();
    }

    public String getKeyId(@KeyType int type) {
        ECCKeyPair keyPair = getPairForType(type);
        return keyPair != null ? keyPair.getKeyId() : null;
    }

    public boolean keyRequireUpdate(@KeyType int type) {
        ECCKeyPair keyPair = getPairForType(type);
        return keyPair == null || keyPair.isExpired();
    }
}
