package br.ufg.inf.hubsaude.model.request;

public class CryptoMaterialConfig {
    private PemConfig pem;
    private Pkcs12Config pkcs12;
    private Pkcs11Config smartcard;
    private Pkcs11Config token;
    private RemoteConfig remote;

    // Getters and Setters
    public PemConfig getPem() { return pem; }
    public void setPem(PemConfig pem) { this.pem = pem; }

    public Pkcs12Config getPkcs12() { return pkcs12; }
    public void setPkcs12(Pkcs12Config pkcs12) { this.pkcs12 = pkcs12; }

    public Pkcs11Config getSmartcard() { return smartcard; }
    public void setSmartcard(Pkcs11Config smartcard) { this.smartcard = smartcard; }

    public Pkcs11Config getToken() { return token; }
    public void setToken(Pkcs11Config token) { this.token = token; }

    public RemoteConfig getRemote() { return remote; }
    public void setRemote(RemoteConfig remote) { this.remote = remote; }

    public static class PemConfig {
        private String privateKey;
        private String password;
        // Getters/Setters
        public String getPrivateKey() { return privateKey; }
        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class Pkcs12Config {
        private String content; // Base64
        private String password;
        private String alias;
        // Getters/Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
    }

    public static class Pkcs11Config {
        private String pin;
        private String identifier;
        private Integer slotId;
        private String tokenLabel;
        // Getters/Setters
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public Integer getSlotId() { return slotId; }
        public void setSlotId(Integer slotId) { this.slotId = slotId; }
        public String getTokenLabel() { return tokenLabel; }
        public void setTokenLabel(String tokenLabel) { this.tokenLabel = tokenLabel; }
    }

    public static class RemoteConfig {
        private String address;
        private String credential;
        // Getters/Setters
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCredential() { return credential; }
        public void setCredential(String credential) { this.credential = credential; }
    }
}
