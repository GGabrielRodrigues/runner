package br.ufg.inf.hubsaude.controller;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.request.ValidationRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import br.ufg.inf.hubsaude.model.fhir.OperationOutcome;
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
               description = "Valida uma assinatura JWS conforme as políticas da ICP-Brasil e do Guia FHIR. Retorna OperationOutcome.")
    public OperationOutcome validate(@RequestBody ValidationRequest request) throws Exception {
        boolean isValid = signatureService.validate(request);
        
        OperationOutcome outcome = new OperationOutcome();
        if (isValid) {
            outcome.addIssue("information", "VALID_SIGNATURE", "A assinatura digital é válida.");
        } else {
            outcome.addIssue("error", "INVALID_SIGNATURE", "A assinatura digital é inválida ou o conteúdo foi alterado.");
        }
        return outcome;
    }
}
