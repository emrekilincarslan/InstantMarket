package com.fondova.finance.repo;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.fondova.finance.App;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.auth.x500.X500Principal;

@Singleton
public class SecretRepository implements EncryptionService {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = SecretRepository.class.getSimpleName();
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";


    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private KeyStore keyStore;
    private App app;


    // ---------------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------------
    @Inject
    public SecretRepository(App app) {
        this.app = app;
        try {
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException |
                CertificateException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    public String encrypt(String alias, String plainText) {
        generateKey(alias);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return encryptRSA_AboveApi23(alias, plainText);
            } else {
                return encryptRSA_BelowApi23(alias, plainText);
            }

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return "";
        }
    }

    public String decrypt(String alias, String encryptedText) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return decryptRSA_AboveApi23(alias, encryptedText);
            } else {
                return decryptRSA_BelowApi23(alias, encryptedText);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return "";
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private void generateKey(String alias) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                generateRSAKey_AboveApi23(alias);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                generateRSAKey_BelowApi23(alias);
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                    NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void generateRSAKey_AboveApi23(String alias) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);


        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec
                .Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build();

        keyPairGenerator.initialize(keyGenParameterSpec);
        keyPairGenerator.generateKeyPair();

    }

    private void generateRSAKey_BelowApi23(String alias) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 100);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(app)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator
                .getInstance("RSA", KEYSTORE_PROVIDER);

        keyPairGenerator.initialize(spec);
        keyPairGenerator.generateKeyPair();
    }


    private String encryptRSA_AboveApi23(String alias, String plainText)
            throws NoSuchAlgorithmException,
            KeyStoreException, NoSuchPaddingException,
            InvalidKeyException, IOException {
        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();

        Cipher cipher = Cipher.getInstance(RSA_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
        cipherOutputStream.write(plainText.getBytes());
        cipherOutputStream.close();

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }


    private String decryptRSA_AboveApi23(String alias, String encryptedText) throws Exception {
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

        Cipher cipher = Cipher.getInstance(RSA_MODE);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT);


        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(decodedBytes), cipher);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }

        return new String(bytes);
    }


    private String encryptRSA_BelowApi23(String alias, String plainText)
            throws NoSuchAlgorithmException, KeyStoreException,
            NoSuchPaddingException, InvalidKeyException, IOException {
        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();

        Cipher cipher = Cipher.getInstance(RSA_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
        cipherOutputStream.write(plainText.getBytes("UTF-8"));
        cipherOutputStream.close();

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    private String decryptRSA_BelowApi23(String alias, String encryptedText) throws Exception {
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

        Cipher cipher = Cipher.getInstance(RSA_MODE);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decodedBytes = Base64.decode(encryptedText.getBytes(), Base64.DEFAULT);

        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(decodedBytes), cipher);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }

        return new String(bytes);
    }

}
