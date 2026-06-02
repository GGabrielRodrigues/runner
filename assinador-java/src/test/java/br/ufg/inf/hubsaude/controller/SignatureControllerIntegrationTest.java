package br.ufg.inf.hubsaude.controller;

import br.ufg.inf.hubsaude.model.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.service.FakeSignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

public class SignatureControllerIntegrationTest {
    private static SignatureController controller;
    private static final int PORT = 8089;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void setUp() throws IOException {
        controller = new SignatureController(new FakeSignatureService());
        controller.startServer(PORT, 0); // Desabilita o timeout durante os testes
    }

    @AfterAll
    public static void tearDown() {
        if (controller != null) {
            controller.stopServer();
        }
    }

    private String generateHash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(encodedHash);
    }

    @Test
    public void testSignEndpointSuccess() throws Exception {
        SignatureRequest request = new SignatureRequest();
        request.setPayloadBase64("ZGFkb3MgZGUgdGVzdGU=");
        request.setSignerName("Dr. Teste");

        String requestJson = mapper.writeValueAsString(request);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/sign"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        SignatureResponse sigResponse = mapper.readValue(response.body(), SignatureResponse.class);
        
        String expectedHash = generateHash(request.getPayloadBase64());
        assertEquals("SUCCESS", sigResponse.getStatus());
        assertEquals("SIMULATED_SIG_" + expectedHash, sigResponse.getSignatureHash());
        assertNotNull(sigResponse.getTimestamp());
    }

    @Test
    public void testSignEndpointMissingPayload() throws Exception {
        SignatureRequest request = new SignatureRequest();
        request.setSignerName("Dr. Teste");
        // Sem payloadBase64

        String requestJson = mapper.writeValueAsString(request);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/sign"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("INVALID_PAYLOAD"));
    }

    @Test
    public void testValidateEndpointValid() throws Exception {
        String payload = "ZGFkb3MgZGUgdGVzdGU=";
        String hash = generateHash(payload);
        String simulatedSignature = "SIMULATED_SIG_" + hash;

        // Montamos o JSON "na mão" para incluir o signatureHash (ou pode-se usar um Map)
        String requestJson = "{\n" +
                "  \"payloadBase64\": \"" + payload + "\",\n" +
                "  \"signerName\": \"Dr. Teste\",\n" +
                "  \"signatureHash\": \"" + simulatedSignature + "\"\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/validate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        SignatureResponse sigResponse = mapper.readValue(response.body(), SignatureResponse.class);
        assertEquals("VALID", sigResponse.getStatus());
        assertEquals(simulatedSignature, sigResponse.getSignatureHash());
    }

    @Test
    public void testValidateEndpointInvalid() throws Exception {
        String payload = "ZGFkb3MgZGUgdGVzdGU=";
        String invalidSignature = "SIMULATED_SIG_badhash123";

        String requestJson = "{\n" +
                "  \"payloadBase64\": \"" + payload + "\",\n" +
                "  \"signerName\": \"Dr. Teste\",\n" +
                "  \"signatureHash\": \"" + invalidSignature + "\"\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/validate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        SignatureResponse sigResponse = mapper.readValue(response.body(), SignatureResponse.class);
        assertEquals("INVALID", sigResponse.getStatus());
        assertEquals(invalidSignature, sigResponse.getSignatureHash());
    }
}