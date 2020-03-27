package com.fondova.finance.repo;

public interface EncryptionService {

    String encrypt(String alias, String plainText);
    String decrypt(String alias, String encryptedText);

}
