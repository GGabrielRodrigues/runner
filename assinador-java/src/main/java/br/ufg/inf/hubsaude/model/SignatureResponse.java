package br.ufg.inf.hubsaude.model;

public class SignatureResponse {
    private String status;
    private String signatureHash;
    private String timestamp;

    public SignatureResponse() {}

    public SignatureResponse(String status, String signatureHash, String timestamp) {
        this.status = status;
        this.signatureHash = signatureHash;
        this.timestamp = timestamp;
    }

    // Getters e Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSignatureHash() { return signatureHash; }
    public void setSignatureHash(String signatureHash) { this.signatureHash = signatureHash; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
