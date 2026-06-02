package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.model.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;

public interface SignatureService {
    
    /**
     * Assina um payload baseado na requisição.
     */
    SignatureResponse sign(SignatureRequest request) throws Exception;
    
    /**
     * Valida uma assinatura existente.
     * Retorna true se for válida, false caso contrário.
     */
    boolean validate(SignatureRequest request, String signatureHash) throws Exception;
}
