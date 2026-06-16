package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.request.ValidationRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;

public interface SignatureService {
    
    /**
     * Assina um payload baseado na requisição.
     */
    SignatureResponse sign(SignatureRequest request) throws Exception;
    
    /**
     * Valida uma assinatura existente baseada na requisição de validação.
     * Retorna true se for válida, false caso contrário.
     */
    boolean validate(ValidationRequest request) throws Exception;
}
