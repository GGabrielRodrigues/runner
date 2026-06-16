package br.ufg.inf.hubsaude.model.request;

import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ValidationRequest {
    private String jwsSignature; // Base64 FHIR standard
    private ValidationConfig operationalConfig;
    private Long referenceTimestamp;
    private String signaturePolicyId;
    private JsonNode originalBundle; // Optional
    private JsonNode originalProvenance; // Optional

    // Getters and Setters
    public String getJwsSignature() { return jwsSignature; }
    public void setJwsSignature(String jwsSignature) { this.jwsSignature = jwsSignature; }
    
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ValidationConfig getOperationalConfig() { return operationalConfig; }
    
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setOperationalConfig(ValidationConfig operationalConfig) { this.operationalConfig = operationalConfig; }
    
    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public void setReferenceTimestamp(Long referenceTimestamp) { this.referenceTimestamp = referenceTimestamp; }
    public String getSignaturePolicyId() { return signaturePolicyId; }
    public void setSignaturePolicyId(String signaturePolicyId) { this.signaturePolicyId = signaturePolicyId; }
    public JsonNode getOriginalBundle() { return originalBundle; }
    public void setOriginalBundle(JsonNode originalBundle) { this.originalBundle = originalBundle; }
    public JsonNode getOriginalProvenance() { return originalProvenance; }
    public void setOriginalProvenance(JsonNode originalProvenance) { this.originalProvenance = originalProvenance; }
}
