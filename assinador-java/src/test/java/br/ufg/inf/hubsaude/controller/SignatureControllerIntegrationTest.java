package br.ufg.inf.hubsaude.controller;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.request.CryptoMaterialConfig;
import br.ufg.inf.hubsaude.model.request.OperationalConfig;
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

    private SignatureRequest createValidRequest() throws Exception {
        SignatureRequest req = new SignatureRequest();
        req.setBundle(mapper.readTree("{\"resourceType\":\"Bundle\"}"));
        req.setProvenance(mapper.readTree("{\"resourceType\":\"Provenance\"}"));
        req.setCryptoMaterial(new CryptoMaterialConfig());
        req.setCertificates(Collections.singletonList("MII..."));
        req.setReferenceTimestamp(1751328001L);
        req.setStrategy("iat");
        req.setSignaturePolicyId("https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.0.1");
        req.setOperationalConfig(new OperationalConfig());
        return req;
    }

    @Test
    public void testSignEndpointSuccess() throws Exception {
        SignatureRequest request = createValidRequest();
        String requestJson = mapper.writeValueAsString(request);

        mockMvc.perform(post("/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.signatureHash").exists());
    }

    @Test
    public void testSignEndpointMissingBundle() throws Exception {
        SignatureRequest request = createValidRequest();
        request.setBundle(null); // Provoca erro de validação

        String requestJson = mapper.writeValueAsString(request);

        mockMvc.perform(post("/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resourceType").value("OperationOutcome"))
                .andExpect(jsonPath("$.issue[0].code").value("MISSING_BUNDLE"));
    }
}
