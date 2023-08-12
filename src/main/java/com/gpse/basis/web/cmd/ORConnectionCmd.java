package com.gpse.basis.web.cmd;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Views;
import com.gpse.basis.domain.credentials.CredentialGroupUnion;
import com.gpse.basis.domain.credentials.PredicateRequirement;
import jakarta.annotation.Nullable;

import java.util.List;

public class ORConnectionCmd {
    private List<CredentialGroupUnion> credentials;
    private List<PredicateRequirement> predicateRequirements;

    @Nullable
    private AttributeRequirementCmd attributeRequirement;

    public ORConnectionCmd(final List<CredentialGroupUnion> credentials, final List<PredicateRequirement> predicateRequirements) {
        this.credentials = credentials;
        this.predicateRequirements = predicateRequirements;
    }

    @JsonView(Views.Public.class)
    public List<CredentialGroupUnion> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<CredentialGroupUnion> credentials) {
        this.credentials = credentials;
    }

    @JsonView(Views.Public.class)
    public List<PredicateRequirement> getPredicateRequirements() {
        return predicateRequirements;
    }

    public void setPredicateRequirements(List<PredicateRequirement> predicateRequirements) {
        this.predicateRequirements = predicateRequirements;
    }

    @JsonView(Views.Public.class)
    public AttributeRequirementCmd getAttributeRequirement() {
        return attributeRequirement;
    }

    public void setAttributeRequirement(AttributeRequirementCmd attributeRequirement) {
        this.attributeRequirement = attributeRequirement;
    }
}
