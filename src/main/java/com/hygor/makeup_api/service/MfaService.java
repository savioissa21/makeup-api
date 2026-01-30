package com.hygor.makeup_api.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData; // Import correto
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);

    public String generateNewSecret() {
        return secretGenerator.generate(); 
    }

   public String getQrCodeUrl(String secret, String email) {
    // CORREÇÃO: Usar o Builder direto para evitar erro de compilação 🕵️‍♀️ ✨
    dev.samstevens.totp.qr.QrData data = new dev.samstevens.totp.qr.QrData.Builder()
            .label(email)
            .secret(secret)
            .issuer("Boutique Hygor & Ana Julia")
            .algorithm(dev.samstevens.totp.code.HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();
    return data.getUri(); 
}

    public boolean verifyCode(String secret, int code) {
        return verifier.isValidCode(secret, String.format("%06d", code));
    }
}