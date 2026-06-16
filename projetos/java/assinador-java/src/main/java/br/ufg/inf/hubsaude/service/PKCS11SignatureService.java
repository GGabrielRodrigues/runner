package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.exception.ValidationException;
import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public final class PKCS11SignatureService implements SignatureService {

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
            String configContent = "--name=Runner\nlibrary=" + libraryPath;
            Provider sunPKCS11 = Security.getProvider("SunPKCS11");
            if (sunPKCS11 == null) {
                throw new ValidationException("PKCS11_NOT_SUPPORTED", "Provider SunPKCS11 não encontrado no JDK.");
            }
            this.pkcs11Provider = sunPKCS11.configure(configContent);
            Security.addProvider(pkcs11Provider);
        } catch (Exception e) {
            throw new ValidationException("PKCS11_INIT_ERROR", "Erro ao inicializar PKCS#11: " + e.getMessage());
        }
    }

    @Override
    public SignatureResponse sign(SignatureRequest request) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
        keyStore.load(null, pin != null ? pin.toCharArray() : null);

        String alias = keyStore.aliases().hasMoreElements() ? keyStore.aliases().nextElement() : null;
        if (alias == null) throw new ValidationException("NO_KEY", "Nenhuma chave encontrada.");

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
        Signature signature = Signature.getInstance("SHA256withRSA", pkcs11Provider);
        signature.initSign(privateKey);
        
        signature.update(request.getBundle().toString().getBytes(StandardCharsets.UTF_8));
        byte[] signedData = signature.sign();

        return new SignatureResponse("SUCCESS", Base64.getEncoder().encodeToString(signedData), java.time.Instant.now().toString());
    }

    @Override
    public boolean validate(br.ufg.inf.hubsaude.model.request.ValidationRequest request) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
        keyStore.load(null, pin != null ? pin.toCharArray() : null);

        String alias = keyStore.aliases().hasMoreElements() ? keyStore.aliases().nextElement() : null;
        if (alias == null) return false;

        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
        Signature signature = Signature.getInstance("SHA256withRSA", pkcs11Provider);
        signature.initVerify(publicKey);

        if (request.getOriginalBundle() != null) {
            signature.update(request.getOriginalBundle().toString().getBytes(StandardCharsets.UTF_8));
        }
        
        return signature.verify(Base64.getDecoder().decode(request.getJwsSignature()));
    }
}
