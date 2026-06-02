package br.ufg.inf.hubsaude;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.model.fhir.OperationOutcome;
import br.ufg.inf.hubsaude.service.SignatureService;
import br.ufg.inf.hubsaude.service.FakeSignatureService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Main {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        if (isCliMode(args)) {
            runCliMode(args);
        } else {
            SpringApplication.run(Main.class, args);
        }
    }

    private static boolean isCliMode(String[] args) {
        if (args.length == 0) return false;
        String cmd = args[0].toLowerCase();
        return cmd.equals("sign") || cmd.equals("validate");
    }

    private static void runCliMode(String[] args) {
        String command = args[0];
        String jsonInput = null;

        for (int i = 1; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                jsonInput = args[i];
                break;
            }
        }

        try {
            // Instância direta para performance no modo CLI (Cold Start)
            FakeSignatureService service = new FakeSignatureService();
            // Precisamos injetar o validador manualmente pois não estamos subindo o contexto Spring
            // Em uma evolução, poderíamos usar um Profile do Spring para carregar o contexto leve.
            
            if (jsonInput == null) {
                printError("MISSING_ARGS", "Forneça o JSON de entrada contendo os 8 componentes FHIR.");
                System.exit(1);
            }

            SignatureRequest request = mapper.readValue(jsonInput, SignatureRequest.class);

            if ("sign".equalsIgnoreCase(command)) {
                SignatureResponse response = service.sign(request);
                System.out.println(mapper.writeValueAsString(response));
            } else if ("validate".equalsIgnoreCase(command)) {
                // Lógica de validação CLI simplificada
                System.out.println("{\"status\":\"TODO\",\"message\":\"Validação CLI em implementação\"}");
            }
            System.exit(0);

        } catch (Exception e) {
            printError("VALIDATION_ERROR", e.getMessage());
            System.exit(1);
        }
    }

    private static void printError(String code, String message) {
        try {
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue("error", code, message);
            System.err.println(mapper.writeValueAsString(outcome));
        } catch (Exception e) {
            System.err.println("{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"fatal\",\"details\":{\"text\":\"Erro crítico\"}}]}");
        }
    }
}
