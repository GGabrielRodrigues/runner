package br.ufg.inf.hubsaude.model.request;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class SignatureRequest {
    private JsonNode bundle;
    private JsonNode provenance;
    private CryptoMaterialConfig cryptoMaterial;
    private List<String> certificates;
    private Long referenceTimestamp;
    private String strategy;
    private String signaturePolicyId;
    private OperationalConfig operationalConfig;

    // Getters and Setters
    public JsonNode getBundle() { return bundle; }
    public void setBundle(JsonNode bundle) { this.bundle = bundle; }

    public JsonNode getProvenance() { return provenance; }
    public void setProvenance(JsonNode provenance) { this.provenance = provenance; }

    public CryptoMaterialConfig getCryptoMaterial() { return cryptoMaterial; }
    public void setCryptoMaterial(CryptoMaterialConfig cryptoMaterial) { this.cryptoMaterial = cryptoMaterial; }

    public List<String> getCertificates() { return certificates; }
    public void setCertificates(List<String> certificates) { this.certificates = certificates; }

    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public void setReferenceTimestamp(Long referenceTimestamp) { this.referenceTimestamp = referenceTimestamp; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public String getSignaturePolicyId() { return signaturePolicyId; }
    public void setSignaturePolicyId(String signaturePolicyId) { this.signaturePolicyId = signaturePolicyId; }

    public OperationalConfig getOperationalConfig() { return operationalConfig; }
    public void setOperationalConfig(OperationalConfig operationalConfig) { this.operationalConfig = operationalConfig; }
}
