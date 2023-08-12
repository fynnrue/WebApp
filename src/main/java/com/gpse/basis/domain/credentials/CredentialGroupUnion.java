package com.gpse.basis.domain.credentials;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Views;

import java.util.Collection;
import java.util.HashSet;

/**
 * This class is used to represent a credential group or a credential
 */
public class CredentialGroupUnion {
    private String name;
    private Long id;
    private Boolean isGroup;

    private Collection<String> attributes;


    public CredentialGroupUnion(String name, Long id, Boolean isGroup, Collection<String> attributes) {
        this.name = name;
        this.id = id;
        this.isGroup = isGroup;
        this.attributes = attributes;
    }

    /**
     * Creates a CredentialGroupUnion from a Credential.
     * @param credential The credential to create the CredentialGroupUnion from
     * @return The created CredentialGroupUnion
     */
    public static CredentialGroupUnion fromCredential(Credential credential) {
        return new CredentialGroupUnion(credential.getName(), credential.getId(), false, credential.getFields());
    }

    /**
     * Creates a CredentialGroupUnion from a CredentialGroup.
     * @param credentialGroup The credential to create the CredentialGroupUnion from
     * @return The created CredentialGroupUnion
     */
    public static CredentialGroupUnion fromCredentialGroup(CredentialGroup credentialGroup) {
        final var attributes = new HashSet<String>();
        credentialGroup.getCredentials().stream().findFirst().ifPresent(
                credential -> attributes.addAll(credential.getFields())
        );
        for (var credential : credentialGroup.getCredentials()) {
            attributes.retainAll(credential.getFields());
        }
        return new CredentialGroupUnion(credentialGroup.getName(), credentialGroup.getId(), true, attributes.stream().toList());
    }

    @JsonView(Views.Public.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView(Views.Public.class)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonView(Views.Public.class)
    public Boolean getGroup() {
        return isGroup;
    }

    public void setGroup(Boolean group) {
        isGroup = group;
    }

    /**
     * Returns the attributes of the CredentialGroupUnion.
     * If the CredentialGroupUnion is a CredentialGroup, the attributes are the intersection of the
     * attributes of the Credentials in the CredentialGroup.
     * <p>
     * If this object originates from an API call, this may be null.
     * @return The attributes of the CredentialGroupUnion
     */
    @JsonView(Views.Public.class)
    public Collection<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<String> attributes) {
        this.attributes = attributes;
    }
}
