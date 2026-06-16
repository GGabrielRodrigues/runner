package br.ufg.inf.hubsaude;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.request.ValidationRequest;
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
            System.exit(runCliMode(args));
        } else {
            SpringApplication.run(Main.class, args);
        }
    }

    private static boolean isCliMode(String[] args) {
        if (args.length == 0) return false;
        String cmd = args[0].toLowerCase(java.util.Locale.ROOT);
        return cmd.equals("sign") || cmd.equals("validate");
    }

    private static int runCliMode(String[] args) {
        String command = args[0];
        String jsonInput = null;

        for (int i = 1; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                jsonInput = args[i];
                break;
            }
        }

        try {
            FakeSignatureService service = new FakeSignatureService();
            
            if (jsonInput == null) {
                printError("MISSING_ARGS", "Forneça o JSON de entrada contendo os componentes FHIR exigidos.");
                return 1;
            }

            if ("sign".equalsIgnoreCase(command)) {
                SignatureRequest request = mapper.readValue(jsonInput, SignatureRequest.class);
                SignatureResponse response = service.sign(request);
                System.out.println(mapper.writeValueAsString(response));
            } else if ("validate".equalsIgnoreCase(command)) {
                ValidationRequest request = mapper.readValue(jsonInput, ValidationRequest.class);
                boolean isValid = service.validate(request);
                
                OperationOutcome outcome = new OperationOutcome();
                if (isValid) {
                    outcome.addIssue("information", "VALID", "Assinatura válida.");
                } else {
                    outcome.addIssue("error", "INVALID", "Assinatura inválida.");
                }
                System.out.println(mapper.writeValueAsString(outcome));
            }
            return 0;

        } catch (Exception e) {
            printError("VALIDATION_ERROR", e.getMessage());
            return 1;
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
