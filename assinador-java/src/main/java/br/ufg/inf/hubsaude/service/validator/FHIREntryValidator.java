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

        validateTimestampRange(request.getReferenceTimestamp());

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

    private void validateTimestampRange(Long timestamp) {
        // Intervalo válido: [1751328000, 4102444800] (1º julho 2025 a 31 dezembro 2099)
        if (timestamp < 1751328000L || timestamp > 4102444800L) {
            throw new ValidationException("INVALID_TIMESTAMP_RANGE", "O timestamp de referência deve estar entre 2025-07-01 e 2099-12-31.");
        }
    }
}
