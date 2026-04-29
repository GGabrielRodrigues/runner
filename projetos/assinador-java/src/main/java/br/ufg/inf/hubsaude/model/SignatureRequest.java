package br.ufg.inf.hubsaude.model;

public class SignatureRequest {
    private String payloadBase64;
    private String signerName;

    // Construtor vazio necessário para o Jackson (JSON)
    public SignatureRequest() {}

    public String getPayloadBase64() {
        return payloadBase64;
    }

    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }

    public String getSignerName() {
        return signerName;
    }

    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }
}
