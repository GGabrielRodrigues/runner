package br.ufg.inf.hubsaude.model.request;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;

public class OperationalConfig {
    private VerificationConfig verification;
    private TrustStoreConfig trustStore;
    private TemporalPolicyConfig temporalPolicy;
    private SecurityConfig security;
    private MiddlewareCryptoConfig middlewareCrypto;

    // Getters and Setters
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public VerificationConfig getVerification() { return verification; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setVerification(VerificationConfig verification) { this.verification = verification; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TrustStoreConfig getTrustStore() { return trustStore; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setTrustStore(TrustStoreConfig trustStore) { this.trustStore = trustStore; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TemporalPolicyConfig getTemporalPolicy() { return temporalPolicy; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setTemporalPolicy(TemporalPolicyConfig temporalPolicy) { this.temporalPolicy = temporalPolicy; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public SecurityConfig getSecurity() { return security; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setSecurity(SecurityConfig security) { this.security = security; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public MiddlewareCryptoConfig getMiddlewareCrypto() { return middlewareCrypto; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setMiddlewareCrypto(MiddlewareCryptoConfig middlewareCrypto) { this.middlewareCrypto = middlewareCrypto; }

    public static class VerificationConfig {
        private Integer ocspCacheTtl;
        private Integer crlCacheTtl;
        private Integer ocspTimeout;
        private Integer crlTimeout;
        private Integer tsaTimeout;
        private Integer maxRetries;
        private Integer retryInterval;
        private String tsaUrl;
        private String tsaUsername;
        private String tsaPassword;
        // Getters/Setters
        public Integer getOcspCacheTtl() { return ocspCacheTtl; }
        public void setOcspCacheTtl(Integer ocspCacheTtl) { this.ocspCacheTtl = ocspCacheTtl; }
        public Integer getCrlCacheTtl() { return crlCacheTtl; }
        public void setCrlCacheTtl(Integer crlCacheTtl) { this.crlCacheTtl = crlCacheTtl; }
        public Integer getOcspTimeout() { return ocspTimeout; }
        public void setOcspTimeout(Integer ocspTimeout) { this.ocspTimeout = ocspTimeout; }
        public Integer getCrlTimeout() { return crlTimeout; }
        public void setCrlTimeout(Integer crlTimeout) { this.crlTimeout = crlTimeout; }
        public Integer getTsaTimeout() { return tsaTimeout; }
        public void setTsaTimeout(Integer tsaTimeout) { this.tsaTimeout = tsaTimeout; }
        public Integer getMaxRetries() { return maxRetries; }
        public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
        public Integer getRetryInterval() { return retryInterval; }
        public void setRetryInterval(Integer retryInterval) { this.retryInterval = retryInterval; }
        public String getTsaUrl() { return tsaUrl; }
        public void setTsaUrl(String tsaUrl) { this.tsaUrl = tsaUrl; }
        public String getTsaUsername() { return tsaUsername; }
        public void setTsaUsername(String tsaUsername) { this.tsaUsername = tsaUsername; }
        public String getTsaPassword() { return tsaPassword; }
        public void setTsaPassword(String tsaPassword) { this.tsaPassword = tsaPassword; }
    }

    public static class TrustStoreConfig {
        private String icpBrasilUrlCertificados;
        private String icpBrasilUrlHash512;
        private Integer timeout;
        private Integer maxRetries;
        private String backoff;
        private String repositorio; // memoria, diretorio, s3
        private String diretorio;
        private String bucket;
        private Integer refresh;
        private Integer ttlCritico;
        private Integer ttlMaximo;
        // Getters/Setters (Omitindo para brevidade na exibição, mas devem ser incluídos no arquivo)
        public String getIcpBrasilUrlCertificados() { return icpBrasilUrlCertificados; }
        public void setIcpBrasilUrlCertificados(String icpBrasilUrlCertificados) { this.icpBrasilUrlCertificados = icpBrasilUrlCertificados; }
        public String getIcpBrasilUrlHash512() { return icpBrasilUrlHash512; }
        public void setIcpBrasilUrlHash512(String icpBrasilUrlHash512) { this.icpBrasilUrlHash512 = icpBrasilUrlHash512; }
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
        public Integer getMaxRetries() { return maxRetries; }
        public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
        public String getBackoff() { return backoff; }
        public void setBackoff(String backoff) { this.backoff = backoff; }
        public String getRepositorio() { return repositorio; }
        public void setRepositorio(String repositorio) { this.repositorio = repositorio; }
        public String getDiretorio() { return diretorio; }
        public void setDiretorio(String diretorio) { this.diretorio = diretorio; }
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public Integer getRefresh() { return refresh; }
        public void setRefresh(Integer refresh) { this.refresh = refresh; }
        public Integer getTtlCritico() { return ttlCritico; }
        public void setTtlCritico(Integer ttlCritico) { this.ttlCritico = ttlCritico; }
        public Integer getTtlMaximo() { return ttlMaximo; }
        public void setTtlMaximo(Integer ttlMaximo) { this.ttlMaximo = ttlMaximo; }
    }

    public static class TemporalPolicyConfig {
        private Long minCertificateDate;
        public Long getMinCertificateDate() { return minCertificateDate; }
        public void setMinCertificateDate(Long minCertificateDate) { this.minCertificateDate = minCertificateDate; }
    }

    public static class SecurityConfig {
        private Integer maxEntriesBundle;
        private Long maxBundleSize;
        private Integer timeoutVerificationBundle;
        // Getters/Setters
        public Integer getMaxEntriesBundle() { return maxEntriesBundle; }
        public void setMaxEntriesBundle(Integer maxEntriesBundle) { this.maxEntriesBundle = maxEntriesBundle; }
        public Long getMaxBundleSize() { return maxBundleSize; }
        public void setMaxBundleSize(Long maxBundleSize) { this.maxBundleSize = maxBundleSize; }
        public Integer getTimeoutVerificationBundle() { return timeoutVerificationBundle; }
        public void setTimeoutVerificationBundle(Integer timeoutVerificationBundle) { this.timeoutVerificationBundle = timeoutVerificationBundle; }
    }

    public static class MiddlewareCryptoConfig {
        private Biblioteca biblioteca;
        private Pkcs11 pkcs11;
        private Sessao sessao;
        private Conectividade conectividade;

        public static class Biblioteca {
            private String caminho;
            private String arquitetura;
            public String getCaminho() { return caminho; }
            public void setCaminho(String caminho) { this.caminho = caminho; }
            public String getArquitetura() { return arquitetura; }
            public void setArquitetura(String arquitetura) { this.arquitetura = arquitetura; }
        }

        public static class Pkcs11 {
            private Integer slotId;
            private String tokenLabel;
            private List<String> mecanismos;
            public Integer getSlotId() { return slotId; }
            public void setSlotId(Integer slotId) { this.slotId = slotId; }
            public String getTokenLabel() { return tokenLabel; }
            public void setTokenLabel(String tokenLabel) { this.tokenLabel = tokenLabel; }
            public List<String> getMecanismos() { return mecanismos == null ? null : new ArrayList<>(mecanismos); }
            public void setMecanismos(List<String> mecanismos) { this.mecanismos = mecanismos == null ? null : new ArrayList<>(mecanismos); }
        }

        public static class Sessao {
            private String modo; // read-only, read-write
            private Integer timeoutInatividade;
            private Integer tentativasAutenticacao;
            public String getModo() { return modo; }
            public void setModo(String modo) { this.modo = modo; }
            public Integer getTimeoutInatividade() { return timeoutInatividade; }
            public void setTimeoutInatividade(Integer timeoutInatividade) { this.timeoutInatividade = timeoutInatividade; }
            public Integer getTentativasAutenticacao() { return tentativasAutenticacao; }
            public void setTentativasAutenticacao(Integer tentativasAutenticacao) { this.tentativasAutenticacao = tentativasAutenticacao; }
        }

        public static class Conectividade {
            private Integer timeoutConexao;
            private Integer intervaloRetry;
            private Integer maximoRetries;
            public Integer getTimeoutConexao() { return timeoutConexao; }
            public void setTimeoutConexao(Integer timeoutConexao) { this.timeoutConexao = timeoutConexao; }
            public Integer getIntervaloRetry() { return intervaloRetry; }
            public void setIntervaloRetry(Integer intervaloRetry) { this.intervaloRetry = intervaloRetry; }
            public Integer getMaximoRetries() { return maximoRetries; }
            public void setMaximoRetries(Integer maximoRetries) { this.maximoRetries = maximoRetries; }
        }

        // Getters/Setters para MiddlewareCryptoConfig
        @SuppressFBWarnings("EI_EXPOSE_REP")
        public Biblioteca getBiblioteca() { return biblioteca; }
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public void setBiblioteca(Biblioteca biblioteca) { this.biblioteca = biblioteca; }

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public Pkcs11 getPkcs11() { return pkcs11; }
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public void setPkcs11(Pkcs11 pkcs11) { this.pkcs11 = pkcs11; }

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public Sessao getSessao() { return sessao; }
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public void setSessao(Sessao sessao) { this.sessao = sessao; }

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public Conectividade getConectividade() { return conectividade; }
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public void setConectividade(Conectividade conectividade) { this.conectividade = conectividade; }
    }
}
