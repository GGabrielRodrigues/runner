package br.ufg.inf.hubsaude.controller;

import br.ufg.inf.hubsaude.exception.ValidationException;
import br.ufg.inf.hubsaude.model.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.service.SignatureService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SignatureController {
    private final SignatureService signatureService;
    private final ObjectMapper mapper;
    private HttpServer server;
    private ScheduledExecutorService timeoutExecutor;
    private final AtomicLong lastActivityTimestamp = new AtomicLong(System.currentTimeMillis());

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
        this.mapper = new ObjectMapper();
    }

    public void startServer(int port, int timeoutMinutes) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/sign", new SignHandler());
        server.createContext("/validate", new ValidateHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado na porta: " + port);

        if (timeoutMinutes > 0) {
            startTimeoutThread(timeoutMinutes);
            System.out.println("Timeout configurado para " + timeoutMinutes + " minutos.");
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        if (timeoutExecutor != null) {
            timeoutExecutor.shutdownNow();
        }
    }

    private void startTimeoutThread(int timeoutMinutes) {
        timeoutExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Timeout-Monitor");
            t.setDaemon(true);
            return t;
        });

        long timeoutMillis = TimeUnit.MINUTES.toMillis(timeoutMinutes);

        timeoutExecutor.scheduleAtFixedRate(() -> {
            long idleTime = System.currentTimeMillis() - lastActivityTimestamp.get();
            if (idleTime > timeoutMillis) {
                System.out.println("Inatividade detectada por mais de " + timeoutMinutes + " minutos. Encerrando servidor.");
                System.exit(0);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void updateActivity() {
        lastActivityTimestamp.set(System.currentTimeMillis());
    }

    private class SignHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            updateActivity();
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            try {
                InputStream is = exchange.getRequestBody();
                SignatureRequest request = mapper.readValue(is, SignatureRequest.class);
                SignatureResponse response = signatureService.sign(request);
                sendResponse(exchange, 200, response);
            } catch (ValidationException e) {
                sendError(exchange, 400, e.getErrorCode(), e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "INTERNAL_ERROR", e.getMessage());
            }
        }
    }

    private class ValidateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            updateActivity();
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            try {
                InputStream is = exchange.getRequestBody();
                byte[] bodyBytes = is.readAllBytes();
                String jsonInput = new String(bodyBytes, StandardCharsets.UTF_8);
                
                SignatureRequest request = mapper.readValue(jsonInput, SignatureRequest.class);
                
                String signatureToVerify = extractHashFromJson(jsonInput);
                
                boolean isValid = signatureService.validate(request, signatureToVerify);
                
                SignatureResponse response = new SignatureResponse(
                        isValid ? "VALID" : "INVALID",
                        signatureToVerify,
                        java.time.Instant.now().toString()
                );
                sendResponse(exchange, 200, response);
            } catch (ValidationException e) {
                sendError(exchange, 400, e.getErrorCode(), e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "INTERNAL_ERROR", e.getMessage());
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object responseObj) throws IOException {
        String jsonResponse = mapper.writeValueAsString(responseObj);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String code, String message) throws IOException {
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("status", "ERROR");
        errorNode.put("errorCode", code);
        errorNode.put("message", message);
        sendResponse(exchange, statusCode, errorNode);
    }

    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Método não permitido. Use POST.");
    }
    
    private String extractHashFromJson(String json) throws Exception {
        JsonNode rootNode = mapper.readTree(json);
        if (rootNode.has("signatureHash")) {
            return rootNode.get("signatureHash").asText();
        }
        return "";
    }
}