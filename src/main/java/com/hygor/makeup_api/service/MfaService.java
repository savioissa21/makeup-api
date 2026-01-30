package com.hygor.makeup_api.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey; // IMPORT CRÍTICO
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateNewSecret() {
        // O método createCredentials() devolve um objeto GoogleAuthenticatorKey
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getSecret(); // Se der erro aqui, tenta limpar o projeto (mvn clean) ⚡
    }

    public String getQrCodeUrl(String secret, String email) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("Boutique Hygor & Ana Julia", email, 
                new GoogleAuthenticatorKey.Builder(secret).build());
    }

    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}