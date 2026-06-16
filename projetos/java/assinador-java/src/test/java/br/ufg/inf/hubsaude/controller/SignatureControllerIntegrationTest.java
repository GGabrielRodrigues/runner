package br.ufg.inf.hubsaude.controller;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.request.ValidationRequest;
import br.ufg.inf.hubsaude.model.request.CryptoMaterialConfig;
import br.ufg.inf.hubsaude.model.request.OperationalConfig;
import br.ufg.inf.hubsaude.model.request.ValidationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SignatureControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

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
        ValidationConfig config = new ValidationConfig();
        config.setTrustStoreIcpBrasil(Collections.singletonList("hash"));
        req.setOperationalConfig(config);
        return req;
    }

    @Test
    public void testSignEndpointSuccess() throws Exception {
        SignatureRequest request = createValidSignRequest();
        String requestJson = mapper.writeValueAsString(request);

        mockMvc.perform(post("/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    public void testValidateEndpointSuccess() throws Exception {
        ValidationRequest request = createValidValidationRequest();
        String requestJson = mapper.writeValueAsString(request);

        mockMvc.perform(post("/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceType").value("OperationOutcome"))
                .andExpect(jsonPath("$.issue[0].code").value("VALID_SIGNATURE"));
    }
}
