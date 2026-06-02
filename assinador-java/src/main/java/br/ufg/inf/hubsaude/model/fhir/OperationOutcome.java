package br.ufg.inf.hubsaude.model.fhir;

import java.util.ArrayList;
import java.util.List;

public class OperationOutcome {
    private String resourceType = "OperationOutcome";
    private List<Issue> issue = new ArrayList<>();

    public void addIssue(String severity, String code, String details) {
        this.issue.add(new Issue(severity, code, details));
    }

    // Getters and Setters
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public List<Issue> getIssue() { return issue; }
    public void setIssue(List<Issue> issue) { this.issue = issue; }

    public static class Issue {
        private String severity; // fatal, error, warning, information
        private String code;     // business code
        private Details details;

        public Issue() {}
        public Issue(String severity, String code, String text) {
            this.severity = severity;
            this.code = code;
            this.details = new Details(text);
        }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Details getDetails() { return details; }
        public void setDetails(Details details) { this.details = details; }

        public static class Details {
            private String text;
            public Details() {}
            public Details(String text) { this.text = text; }
            public String getText() { return text; }
            public void setText(String text) { this.text = text; }
        }
    }
}
