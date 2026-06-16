package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.request.ValidationRequest;
import br.ufg.inf.hubsaude.model.request.CryptoMaterialConfig;
import br.ufg.inf.hubsaude.model.request.OperationalConfig;
import br.ufg.inf.hubsaude.model.request.ValidationConfig;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.service.validator.FHIREntryValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FakeSignatureServiceTest {

    private FakeSignatureService service;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new FakeSignatureService();
        FHIREntryValidator validator = new FHIREntryValidator();
        ReflectionTestUtils.setField(service, "validator", validator);
    }

    private SignatureRequest createValidSignRequest() throws Exception {
        SignatureRequest req = new SignatureRequest();
        req.setBundle(mapper.readTree("{\"resourceType\":\"Bundle\"}"));
        req.setProvenance(mapper.readTree("{\"resourceType\":\"Provenance\"}"));
        req.setCryptoMaterial(new CryptoMaterialConfig());
        req.setCertificates(Collections.singletonList("MII..."));
        req.setReferenceTimestamp(System.currentTimeMillis() / 1000);
        req.setStrategy("iat");
        req.setSignaturePolicyId("https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.0.1");
        req.setOperationalConfig(new OperationalConfig());
        return req;
    }

    private ValidationRequest createValidValidationRequest() throws Exception {
        ValidationRequest req = new ValidationRequest();
        req.setJwsSignature("SIMULATED_SIG_abc123");
        req.setReferenceTimestamp(System.currentTimeMillis() / 1000);
        req.setSignaturePolicyId("policy-uri");
        req.setOperationalConfig(new ValidationConfig());
        return req;
    }

    @Test
    void testSign_Success() throws Exception {
        SignatureRequest req = createValidSignRequest();
        SignatureResponse response = service.sign(req);

        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getSignatureHash().startsWith("SIMULATED_SIG_"));
    }

    @Test
    void testValidate_Success() throws Exception {
        ValidationRequest req = createValidValidationRequest();
        boolean isValid = service.validate(req);
        assertTrue(isValid);
    }
}
