package br.ufg.inf.hubsaude.controller;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.service.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Assinatura Digital FHIR", description = "Endpoints para criação e validação de assinaturas em conformidade com o Guia da SES-GO")
public class SignatureController {

    private final SignatureService signatureService;

    @Autowired
    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @PostMapping("/sign")
    @Operation(summary = "Criar Assinatura Digital (Passo 1 a 14)", 
               description = "Recebe os 8 componentes FHIR e retorna o recurso Signature JWS ou OperationOutcome em caso de erro.")
    public SignatureResponse sign(@RequestBody SignatureRequest request) throws Exception {
        return signatureService.sign(request);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validar Assinatura Digital", 
               description = "Valida uma assinatura JWS conforme as políticas da ICP-Brasil e do Guia FHIR.")
    public SignatureResponse validate(@RequestBody SignatureRequest request) throws Exception {
        // Para a simulação, assumimos que o signatureHash está vindo dentro do JSON
        // ou que a lógica do FakeSignatureService cuidará disso.
        boolean isValid = signatureService.validate(request, "SIMULATED_SIG_MOCKED");
        
        return new SignatureResponse(
                isValid ? "VALID" : "INVALID",
                "SIMULATED_SIG_MOCKED",
                java.time.Instant.now().toString()
        );
    }
}
