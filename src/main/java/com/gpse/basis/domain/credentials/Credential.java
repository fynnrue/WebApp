package com.gpse.basis.domain.credentials;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Views;
import jakarta.persistence.*;

import java.util.Collection;

@Entity
public class Credential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    @JsonView(Views.Public.class)
    private Long id;

    @Column
    @JsonView(Views.Public.class)
    private String name;

    @Column
    @JsonView(Views.Public.class)
    private String origin;

    @Column
    @JsonView(Views.Public.class)
    private String additional;

    @Column
    @JsonView(Views.Public.class)
    private String credentialDid;

    @ElementCollection(fetch = FetchType.EAGER)
    @JsonView(Views.Public.class)
    private Collection<String> checklist;

    @ElementCollection
    @JsonView(Views.Public.class)
    private Collection<String> fields;

    public Credential(
            final String credentialDid,
            final Collection<String> checklist,
            final Collection<String> fields,
            final String name,
            final String origin,
            final String additional
    ) {
        this.credentialDid = credentialDid;
        this.checklist = checklist;
        this.fields = fields;
        this.name = name;
        this.origin = origin;
        this.additional = additional;
    }

    public Credential() {
    }

    public Credential(String did, Collection<String> checklist, Collection<String> attributes, String fancyName) {
    this.credentialDid = did;
    this.checklist = checklist;
    this.fields = attributes;
    this.name = fancyName;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getCredentialDid() {
        return credentialDid;
    }

    public void setCredentialDid(String credentialDid) {
        this.credentialDid = credentialDid;
    }

    public Collection<String> getChecklist() {
        return checklist;
    }

    public void setChecklist(Collection<String> checklist) {
        this.checklist = checklist;
    }

    public Collection<String> getFields() {
        return fields;
    }

    public void setFields(Collection<String> fields) {
        this.fields = fields;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }
}
