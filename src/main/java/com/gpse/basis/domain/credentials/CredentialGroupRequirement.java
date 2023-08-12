package com.gpse.basis.domain.credentials;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.Collection;

public class CredentialGroupRequirement {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private CredentialGroup group;

    @OneToMany
    private Collection<CredentialRequirement> requirements;


    public CredentialGroup getGroup() {
        return group;
    }

    public void setGroup(CredentialGroup group) {
        this.group = group;
    }

    public Collection<CredentialRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(Collection<CredentialRequirement> requirements) {
        this.requirements = requirements;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
