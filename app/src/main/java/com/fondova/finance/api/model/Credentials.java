package com.fondova.finance.api.model;

public class Credentials {
    public String username;
    public String password;

    public static Credentials create(String username, String password) {
        Credentials creds = new Credentials();
        creds.username = username;
        creds.password = password;
        return creds;
    }
}
