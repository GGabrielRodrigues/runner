package br.ufg.inf.hubsaude.model.request;

import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
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
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public JsonNode getBundle() { return bundle; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setBundle(JsonNode bundle) { this.bundle = bundle; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public JsonNode getProvenance() { return provenance; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setProvenance(JsonNode provenance) { this.provenance = provenance; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public CryptoMaterialConfig getCryptoMaterial() { return cryptoMaterial; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setCryptoMaterial(CryptoMaterialConfig cryptoMaterial) { this.cryptoMaterial = cryptoMaterial; }

    public List<String> getCertificates() { return certificates == null ? null : new ArrayList<>(certificates); }
    public void setCertificates(List<String> certificates) { this.certificates = certificates == null ? null : new ArrayList<>(certificates); }

    public Long getReferenceTimestamp() { return referenceTimestamp; }
    public void setReferenceTimestamp(Long referenceTimestamp) { this.referenceTimestamp = referenceTimestamp; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public String getSignaturePolicyId() { return signaturePolicyId; }
    public void setSignaturePolicyId(String signaturePolicyId) { this.signaturePolicyId = signaturePolicyId; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public OperationalConfig getOperationalConfig() { return operationalConfig; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setOperationalConfig(OperationalConfig operationalConfig) { this.operationalConfig = operationalConfig; }
}
