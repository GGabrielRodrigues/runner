package br.ufg.inf.hubsaude.model.request;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;

public class ValidationConfig {
    private List<String> trustStoreIcpBrasil; // hashes SHA-256
    private Long minCertIssueDate;
    private Timeouts timeouts;
    private Long revocationCacheTtl;
    private Integer nearExpiryThresholdDays;
    private Integer signatureAgeThresholdDays;
    private String revocationPolicy; // strict, soft-fail, warn
    private String ocspUnknownHandling; // treat-as-revoked, treat-as-warning
    private SecurityLimits securityLimits;

    public static class Timeouts {
        private Integer ocsp;
        private Integer crl;
        private Integer tsa;
        // Getters/Setters
        public Integer getOcsp() { return ocsp; }
        public void setOcsp(Integer ocsp) { this.ocsp = ocsp; }
        public Integer getCrl() { return crl; }
        public void setCrl(Integer crl) { this.crl = crl; }
        public Integer getTsa() { return tsa; }
        public void setTsa(Integer tsa) { this.tsa = tsa; }
    }

    public static class SecurityLimits {
        private Integer maxEntriesBundle;
        private Long maxBundleBytes;
        private Integer bundleVerifyTimeout;
        // Getters/Setters
        public Integer getMaxEntriesBundle() { return maxEntriesBundle; }
        public void setMaxEntriesBundle(Integer maxEntriesBundle) { this.maxEntriesBundle = maxEntriesBundle; }
        public Long getMaxBundleBytes() { return maxBundleBytes; }
        public void setMaxBundleBytes(Long maxBundleBytes) { this.maxBundleBytes = maxBundleBytes; }
        public Integer getBundleVerifyTimeout() { return bundleVerifyTimeout; }
        public void setBundleVerifyTimeout(Integer bundleVerifyTimeout) { this.bundleVerifyTimeout = bundleVerifyTimeout; }
    }

    // Getters and Setters
    public List<String> getTrustStoreIcpBrasil() { return trustStoreIcpBrasil == null ? null : new ArrayList<>(trustStoreIcpBrasil); }
    public void setTrustStoreIcpBrasil(List<String> trustStoreIcpBrasil) { this.trustStoreIcpBrasil = trustStoreIcpBrasil == null ? null : new ArrayList<>(trustStoreIcpBrasil); }

    public Long getMinCertIssueDate() { return minCertIssueDate; }
    public void setMinCertIssueDate(Long minCertIssueDate) { this.minCertIssueDate = minCertIssueDate; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Timeouts getTimeouts() { return timeouts; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setTimeouts(Timeouts timeouts) { this.timeouts = timeouts; }

    public Long getRevocationCacheTtl() { return revocationCacheTtl; }
    public void setRevocationCacheTtl(Long revocationCacheTtl) { this.revocationCacheTtl = revocationCacheTtl; }
    public Integer getNearExpiryThresholdDays() { return nearExpiryThresholdDays; }
    public void setNearExpiryThresholdDays(Integer nearExpiryThresholdDays) { this.nearExpiryThresholdDays = nearExpiryThresholdDays; }
    public Integer getSignatureAgeThresholdDays() { return signatureAgeThresholdDays; }
    public void setSignatureAgeThresholdDays(Integer signatureAgeThresholdDays) { this.signatureAgeThresholdDays = signatureAgeThresholdDays; }
    public String getRevocationPolicy() { return revocationPolicy; }
    public void setRevocationPolicy(String revocationPolicy) { this.revocationPolicy = revocationPolicy; }
    public String getOcspUnknownHandling() { return ocspUnknownHandling; }
    public void setOcspUnknownHandling(String ocspUnknownHandling) { this.ocspUnknownHandling = ocspUnknownHandling; }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public SecurityLimits getSecurityLimits() { return securityLimits; }
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setSecurityLimits(SecurityLimits securityLimits) { this.securityLimits = securityLimits; }
}
