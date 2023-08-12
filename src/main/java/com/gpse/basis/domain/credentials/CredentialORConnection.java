package com.gpse.basis.domain.credentials;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Views;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Entity which stores the OR-Connections of Credentials. This is necessary
 * since it is impossible to store a two-dimensional Collection of Entities in an Entity.
 * I am not proud of this solution.
 * Do not worry it works fine
 */
@Entity
public class CredentialORConnection {
    @Id
    @GeneratedValue
    @JsonView(Views.Public.class)
    private Long id;

    @ManyToMany
    @JsonView(Views.Public.class)
    private Collection<Credential> credentials;

    @ElementCollection
    @Nullable
    @JsonView(Views.Public.class)
    private List<PredicateRequirement> predicateRequirements;

    @Column
    @Nullable
    @JsonView(Views.Public.class)
    private AttributeRequirement attributeRequirements;

    @ManyToMany
    @JsonView(Views.Public.class)
    private Collection<CredentialGroup> groups;

    public CredentialORConnection(
            final Collection<Credential> credentials,
            final Collection<CredentialGroup> groups,
            final List<PredicateRequirement> requirements,
            final AttributeRequirement attributeRequirements
    ) {
        this.credentials = credentials;
        this.groups = groups;
        this.predicateRequirements = requirements;
        this.attributeRequirements = attributeRequirements;
    }

    public CredentialORConnection() {
    }

    public Collection<Credential> getCredentials() {
        return credentials;
    }

    @JsonIgnore
    public Collection<Credential> getAllCredentials() {
        return Stream.concat(
                credentials.stream(),
                groups.stream().flatMap(group -> group.getCredentials().stream())
        ).toList();
    }

    public void setCredentials(Collection<Credential> credentials) {
        this.credentials = credentials;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<PredicateRequirement> getRequirements() {
        return predicateRequirements;
    }

    public void setRequirements(List<PredicateRequirement> requirements) {
        this.predicateRequirements = requirements;
    }

    public Collection<CredentialGroup> getGroups() {
        return groups;
    }

    public void setGroups(Collection<CredentialGroup> groups) {
        this.groups = groups;
    }

    public AttributeRequirement getAttributeRequirements() {
        return attributeRequirements;
    }

    public void setAttributeRequirements(AttributeRequirement attributeRequirements) {
        this.attributeRequirements = attributeRequirements;
    }
}
