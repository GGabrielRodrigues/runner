package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.exception.ValidationException;
import br.ufg.inf.hubsaude.model.SignatureRequest;

public class RequestValidator {

    public static void validate(SignatureRequest request) {
        if (request == null) {
            throw new ValidationException("NULL_REQUEST", "A requisição não pode ser nula.");
        }

        if (request.getPayloadBase64() == null || request.getPayloadBase64().isBlank()) {
            throw new ValidationException("INVALID_PAYLOAD", "O campo 'payloadBase64' é obrigatório.");
        }

        if (request.getSignerName() == null || request.getSignerName().isBlank()) {
            throw new ValidationException("INVALID_SIGNER", "O campo 'signerName' é obrigatório.");
        }
    }

    public static void validateForVerification(SignatureRequest request, String signatureHash) {
        validate(request);
        if (signatureHash == null || signatureHash.isBlank()) {
            throw new ValidationException("MISSING_SIGNATURE", "Para validar, é necessário fornecer o 'signatureHash'.");
        }
    }
}
