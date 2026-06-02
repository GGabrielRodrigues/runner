package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.service.validator.FHIREntryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class FakeSignatureService implements SignatureService {

    @Autowired
    private FHIREntryValidator validator;

    @Override
    public SignatureResponse sign(SignatureRequest request) throws Exception {
        // 1. Valida a entrada (Passo 1 do Processo FHIR)
        validator.validate(request);

        // 2. Simulação de processamento (Impressão digital do bundle)
        String payload = request.getBundle().toString();
        String hash = generateHash(payload);

        return new SignatureResponse(
                "SUCCESS",
                "SIMULATED_SIG_" + hash,
                Instant.now().toString()
        );
    }

    @Override
    public boolean validate(SignatureRequest request, String signatureHash) throws Exception {
        // Para simular, apenas verificamos se o hash do bundle corresponde à parte da assinatura
        String payload = request.getBundle().toString();
        String expectedHash = "SIMULATED_SIG_" + generateHash(payload);
        return expectedHash.equals(signatureHash);
    }

    private String generateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(encodedHash);
    }
}
