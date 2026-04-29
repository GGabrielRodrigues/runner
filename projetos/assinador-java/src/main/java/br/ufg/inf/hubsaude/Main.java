package br.ufg.inf.hubsaude;

import br.ufg.inf.hubsaude.exception.ValidationException;
import br.ufg.inf.hubsaude.model.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.service.FakeSignatureService;
import br.ufg.inf.hubsaude.service.SignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class Main {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        // 1. Verificação básica de argumentos
        if (args.length < 2) {
            printError("MISSING_ARGS", "Uso esperado: java -jar assinador.jar <comando> <json>");
            System.exit(1);
        }

        String command = args[0];
        String jsonInput = args[1];

        try {
            // 2. Parse do JSON de entrada para o POJO
            SignatureRequest request = mapper.readValue(jsonInput, SignatureRequest.class);
            SignatureService service = new FakeSignatureService();

            // 3. Execução do comando solicitado
            if ("sign".equalsIgnoreCase(command)) {
                SignatureResponse response = service.sign(request);
                System.out.println(mapper.writeValueAsString(response));
            } 
            else if ("validate".equalsIgnoreCase(command)) {
                // Para o validate, precisamos da assinatura que veio no JSON
                // O Jackson já mapeou o campo 'signatureHash' se o Go enviou
                
                // Extraímos o hash do próprio campo que adicionamos ao modelo/contrato
                // Mas o método da interface pede o hash separado
                String signatureToVerify = extractHashFromJson(jsonInput);
                
                boolean isValid = service.validate(request, signatureToVerify);
                
                SignatureResponse response = new SignatureResponse(
                    isValid ? "VALID" : "INVALID",
                    signatureToVerify,
                    java.time.Instant.now().toString()
                );
                System.out.println(mapper.writeValueAsString(response));
            } 
            else {
                throw new ValidationException("UNKNOWN_COMMAND", "Comando '" + command + "' não reconhecido.");
            }

        } catch (ValidationException e) {
            // Erros de negócio controlados (vão para o stderr)
            printError(e.getErrorCode(), e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            // Erros inesperados (ex: JSON malformado)
            printError("INTERNAL_ERROR", e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Auxiliar para imprimir o JSON de erro no System.err conforme o contrato.
     */
    private static void printError(String code, String message) {
        try {
            ObjectNode errorNode = mapper.createObjectNode();
            errorNode.put("status", "ERROR");
            errorNode.put("errorCode", code);
            errorNode.put("message", message);
            System.err.println(mapper.writeValueAsString(errorNode));
        } catch (Exception e) {
            System.err.println("{\"status\":\"ERROR\",\"message\":\"Erro crítico ao serializar erro.\"}");
        }
    }

    /**
     * Auxiliar para pegar o signatureHash do JSON bruto, 
     * caso o POJO não queira carregar esse campo durante o 'sign'.
     */
	private static String extractHashFromJson(String json) throws Exception {
        JsonNode rootNode = mapper.readTree(json);
        if (rootNode.has("signatureHash")) {
            return rootNode.get("signatureHash").asText();
        }
        return ""; 
	}
}
