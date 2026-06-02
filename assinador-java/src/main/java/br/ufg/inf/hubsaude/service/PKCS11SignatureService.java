package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.exception.ValidationException;
import br.ufg.inf.hubsaude.model.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class PKCS11SignatureService implements SignatureService {

    private final String libraryPath;
    private final String pin;
    private Provider pkcs11Provider;

    public PKCS11SignatureService(String libraryPath, String pin) {
        this.libraryPath = libraryPath;
        this.pin = pin;
        initialize();
    }

    private void initialize() {
        try {
            // O prefixo '--' é OBRIGATÓRIO para o Java interpretar a string como configuração
            String configContent = "--name=Runner\n" +
                                   "library=" + libraryPath;
            
            Provider sunPKCS11 = Security.getProvider("SunPKCS11");
            if (sunPKCS11 == null) {
                throw new ValidationException("PKCS11_NOT_SUPPORTED", "Provider SunPKCS11 não encontrado no JDK.");
            }
            
            this.pkcs11Provider = sunPKCS11.configure(configContent);
            Security.addProvider(pkcs11Provider);
        } catch (Exception e) {
            throw new ValidationException("PKCS11_INIT_ERROR", "Falha ao inicializar PKCS#11 com a biblioteca: " + libraryPath + ". Erro: " + e.getMessage());
        }
    }

    private boolean isPinIncorrect(Throwable e) {
        while (e != null) {
            if (e.getMessage() != null && e.getMessage().contains("CKR_PIN_INCORRECT")) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    @Override
    public SignatureResponse sign(SignatureRequest request) throws Exception {
        RequestValidator.validate(request);

        KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
        try {
            keyStore.load(null, pin != null ? pin.toCharArray() : null);
        } catch (Exception e) {
            if (isPinIncorrect(e)) {
                throw new ValidationException("INVALID_PIN", "PIN incorreto para o dispositivo criptográfico.");
            }
            throw new ValidationException("KEYSTORE_ERROR", "Erro ao carregar KeyStore PKCS#11: " + e.getMessage());
        }

        // Tenta encontrar uma chave privada. No mundo real, usaríamos o signerName ou um alias específico.
        // Para este desafio, pegamos a primeira chave privada disponível para simular a delegação.
        String alias = null;
        if (keyStore.aliases().hasMoreElements()) {
            alias = keyStore.aliases().nextElement();
        }
        
        if (alias == null) {
            throw new ValidationException("NO_KEY_FOUND", "Nenhuma chave encontrada no dispositivo PKCS#11.");
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
        
        Signature signature = Signature.getInstance("SHA256withRSA", pkcs11Provider);
        signature.initSign(privateKey);
        
        byte[] data = Base64.getDecoder().decode(request.getPayloadBase64());
        signature.update(data);
        byte[] signedData = signature.sign();

        String hash = Base64.getEncoder().encodeToString(signedData);
        
        return new SignatureResponse(
                "SUCCESS",
                hash,
                java.time.Instant.now().toString()
        );
    }

    @Override
    public boolean validate(SignatureRequest request, String signatureHash) throws Exception {
        RequestValidator.validateForVerification(request, signatureHash);

        KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
        try {
            keyStore.load(null, pin != null ? pin.toCharArray() : null);
        } catch (Exception e) {
            if (isPinIncorrect(e)) {
                throw new ValidationException("INVALID_PIN", "PIN incorreto para o dispositivo criptográfico.");
            }
            throw new ValidationException("KEYSTORE_ERROR", "Erro ao carregar KeyStore PKCS#11: " + e.getMessage());
        }

        String alias = null;
        if (keyStore.aliases().hasMoreElements()) {
            alias = keyStore.aliases().nextElement();
        }
        
        if (alias == null) {
            throw new ValidationException("NO_KEY_FOUND", "Nenhuma chave encontrada no dispositivo PKCS#11.");
        }

        java.security.cert.Certificate cert = keyStore.getCertificate(alias);
        PublicKey publicKey = cert.getPublicKey();

        Signature signature = Signature.getInstance("SHA256withRSA", pkcs11Provider);
        signature.initVerify(publicKey);

        byte[] data = Base64.getDecoder().decode(request.getPayloadBase64());
        signature.update(data);
        
        byte[] sigBytes = Base64.getDecoder().decode(signatureHash);
        return signature.verify(sigBytes);
    }
}
