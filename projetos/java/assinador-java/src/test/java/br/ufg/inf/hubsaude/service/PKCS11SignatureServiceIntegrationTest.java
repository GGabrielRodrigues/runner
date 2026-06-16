package br.ufg.inf.hubsaude.service;

import br.ufg.inf.hubsaude.model.request.SignatureRequest;
import br.ufg.inf.hubsaude.model.SignatureResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requer biblioteca PKCS#11 nativa configurada")
class PKCS11SignatureServiceIntegrationTest {

    @Test
    void testSignWithPKCS11() throws Exception {
        // Teste desabilitado pois depende de hardware/ambiente específico
    }
}
