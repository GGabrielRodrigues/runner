package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.exception.ValidationException;
import br.ufg.inf.hubsaude.model.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeSignatureServiceTest {

    private FakeSignatureService service;

    // Roda antes de cada teste para garantir um serviço limpo
    @BeforeEach
    void setUp() {
        service = new FakeSignatureService();
    }

    // --- Testes do Comando SIGN ---

    @Test
    void testSign_Success() throws Exception {
        SignatureRequest req = new SignatureRequest();
        req.setPayloadBase64("SGVsbG8gV29ybGQ="); // "Hello World" em base64
        req.setSignerName("Gabriel");

        SignatureResponse response = service.sign(req);

        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getSignatureHash());
        assertTrue(response.getSignatureHash().startsWith("SIMULATED_SIG_"));
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSign_NullPayload_ThrowsException() {
        SignatureRequest req = new SignatureRequest();
        req.setSignerName("Gabriel");
        // Omitindo o payloadBase64 de propósito

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.sign(req);
        });

        assertEquals("INVALID_PAYLOAD", exception.getErrorCode());
    }

    @Test
    void testSign_EmptySignerName_ThrowsException() {
        SignatureRequest req = new SignatureRequest();
        req.setPayloadBase64("SGVsbG8=");
        req.setSignerName(""); // Vazio de propósito

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.sign(req);
        });

        assertEquals("INVALID_SIGNER", exception.getErrorCode());
    }

    // --- Testes do Comando VALIDATE ---

    @Test
    void testValidate_Success() throws Exception {
        // 1. Primeiro assinamos para ter um hash válido
        SignatureRequest req = new SignatureRequest();
        req.setPayloadBase64("SGVsbG8=");
        req.setSignerName("Gabriel");
        SignatureResponse signResponse = service.sign(req);

        // 2. Agora tentamos validar esse hash com o mesmo request
        boolean isValid = service.validate(req, signResponse.getSignatureHash());

        assertTrue(isValid, "A assinatura deveria ser válida para o mesmo payload.");
    }

    @Test
    void testValidate_WrongHash_ReturnsFalse() throws Exception {
        SignatureRequest req = new SignatureRequest();
        req.setPayloadBase64("SGVsbG8=");
        req.setSignerName("Gabriel");

        // Hash incorreto/forjado
        boolean isValid = service.validate(req, "SIMULATED_SIG_1234567890abcdef");

        assertFalse(isValid, "A assinatura deveria ser inválida.");
    }

    @Test
    void testValidate_MissingHash_ThrowsException() {
        SignatureRequest req = new SignatureRequest();
        req.setPayloadBase64("SGVsbG8=");
        req.setSignerName("Gabriel");

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.validate(req, null); // Passando nulo de propósito
        });

        assertEquals("MISSING_SIGNATURE", exception.getErrorCode());
    }
}
