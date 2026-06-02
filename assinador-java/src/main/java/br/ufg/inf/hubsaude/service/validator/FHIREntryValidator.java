package br.ufg.inf.hubsaude.service.validator;

import br.ufg.inf.hubsaude.exception.ValidationException;
import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import org.springframework.stereotype.Component;

@Component
public class FHIREntryValidator {

    public void validate(SignatureRequest request) {
        if (request == null) {
            throw new ValidationException("NULL_INPUT", "A entrada não pode ser nula.");
        }

        if (request.getBundle() == null) {
            throw new ValidationException("MISSING_BUNDLE", "O componente Bundle (E1) é obrigatório.");
        }

        if (request.getProvenance() == null) {
            throw new ValidationException("MISSING_PROVENANCE", "O componente Provenance (E2) é obrigatório.");
        }

        if (request.getCryptoMaterial() == null) {
            throw new ValidationException("MISSING_CRYPTO_MATERIAL", "O material criptográfico (E3) é obrigatório.");
        }

        if (request.getCertificates() == null || request.getCertificates().isEmpty()) {
            throw new ValidationException("MISSING_CERTIFICATES", "A cadeia de certificados (E4) é obrigatória.");
        }

        if (request.getReferenceTimestamp() == null) {
            throw new ValidationException("MISSING_TIMESTAMP", "O timestamp de referência (E5) é obrigatório.");
        }

        validateTimestampRange(request.getReferenceTimestamp(), true);

        if (request.getStrategy() == null || (!request.getStrategy().equals("iat") && !request.getStrategy().equals("tsa"))) {
            throw new ValidationException("INVALID_STRATEGY", "A estratégia (E6) deve ser 'iat' ou 'tsa'.");
        }

        if (request.getSignaturePolicyId() == null || request.getSignaturePolicyId().isBlank()) {
            throw new ValidationException("MISSING_POLICY", "A identificação da política (E7) é obrigatória.");
        }

        if (request.getOperationalConfig() == null) {
            throw new ValidationException("MISSING_CONFIG", "As configurações operacionais (E8) são obrigatórias.");
        }
    }

    public void validateForVerification(br.ufg.inf.hubsaude.model.request.ValidationRequest request) {
        if (request == null) {
            throw new ValidationException("NULL_INPUT", "A entrada de validação não pode ser nula.");
        }

        if (request.getJwsSignature() == null || request.getJwsSignature().isBlank()) {
            throw new ValidationException("MISSING_JWS", "A assinatura JWS (item 1) é obrigatória.");
        }

        if (request.getOperationalConfig() == null) {
            throw new ValidationException("MISSING_CONFIG", "As configurações operacionais (item 2) são obrigatórias.");
        }

        if (request.getReferenceTimestamp() == null) {
            throw new ValidationException("MISSING_TIMESTAMP", "O timestamp de referência (item 3) é obrigatório.");
        }

        validateTimestampRange(request.getReferenceTimestamp(), false);

        if (request.getSignaturePolicyId() == null || request.getSignaturePolicyId().isBlank()) {
            throw new ValidationException("MISSING_POLICY", "A política de assinatura (item 4) é obrigatória.");
        }
    }

    private void validateTimestampRange(Long timestamp, boolean isCreation) {
        // Intervalo básico: [1751328000, 4102444800]
        if (timestamp < 1751328000L || timestamp > 4102444800L) {
            throw new ValidationException("INVALID_TIMESTAMP_RANGE", "O timestamp de referência deve estar entre 2025-07-01 e 2099-12-31.");
        }

        // Regra de Drift: ±300s (5 minutos) em relação ao instante atual
        long now = System.currentTimeMillis() / 1000;
        if (Math.abs(now - timestamp) > 300) {
            String op = isCreation ? "criação" : "validação";
            throw new ValidationException("TIMESTAMP_DRIFT", 
                String.format("O timestamp de %s (%d) diverge mais de 5 minutos do relógio do servidor (%d).", op, timestamp, now));
        }
    }
}
