package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.model.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

public class FakeSignatureService implements SignatureService {

    @Override
    public SignatureResponse sign(SignatureRequest request) throws Exception {
        // 1. Valida a entrada
        RequestValidator.validate(request);

        // 2. Gera o Hash SHA-256 do payload
        String hash = generateHash(request.getPayloadBase64());
        String simulatedSignature = "SIMULATED_SIG_" + hash;

        // 3. Monta a resposta
        return new SignatureResponse(
                "SUCCESS",
                simulatedSignature,
                Instant.now().toString()
        );
    }

    @Override
    public boolean validate(SignatureRequest request, String signatureHash) throws Exception {
        // 1. Valida se temos os dados necessários
        RequestValidator.validateForVerification(request, signatureHash);

        // 2. Recalcula o hash do payload enviado
        String currentHash = "SIMULATED_SIG_" + generateHash(request.getPayloadBase64());

        // 3. Compara com a assinatura fornecida
        return currentHash.equals(signatureHash);
    }

    private String generateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        
        // HexFormat é uma utilidade do Java 17+ muito prática
		return HexFormat.of().formatHex(encodedHash);
	}
}
