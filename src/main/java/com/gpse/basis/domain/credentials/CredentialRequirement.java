package com.gpse.basis.domain.credentials;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.Collection;


// @Entity
public class CredentialRequirement {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Credential credential;

    @ElementCollection
    private Collection<PredicateRequirement> requirements;

    public CredentialRequirement(
            final Credential credential,
            final Collection<PredicateRequirement> requirements
    ) {
        this.credential = credential;
        this.requirements = requirements;
    }

    public CredentialRequirement() {}

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public Collection<PredicateRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(Collection<PredicateRequirement> requirements) {
        this.requirements = requirements;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
