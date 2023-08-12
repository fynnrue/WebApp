package com.gpse.basis.domain.credentials;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.*;

@Entity
public class CredentialGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;


    @ManyToMany
    @Column(unique = true)
    private Collection<Credential> Credentials;

    @Column
    private String credentialString;

    @Column(unique = true)
    private String name;

    @Column
    private String origin;

    @Column
    private String additional;

    public CredentialGroup() {
    }


    public CredentialGroup(Collection <Credential> credentials, String name, String origin, String additional) {
        this.Credentials = credentials;
        this.name = name;
        this.origin = origin;
        this.additional = additional;
        //this.CredentialString = getCredentialsAsString();
    }

    public CredentialGroup(String credentialIDs) {
        this.credentialString = credentialIDs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<Credential> getCredentials() {
        return Credentials;
    }

    public void setCredentials(Collection<Credential> credentials) {
        Credentials = credentials;
    }
    public String getCredentialString() {
        return credentialString;
    }

    public void setCredentialString(String credentialIDs) {
        this.credentialString = credentialIDs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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


    public void testFunction() {
        List <Integer> list = new ArrayList<>();
    }


    @JsonIgnore
    public List<Integer> getCredentialIDSasInt() {
        List <Integer> list = new ArrayList<>();
        String[] arr = getCredentialString().split(",");
        for(int i = 0; i < arr.length; i++) {
            //int num = Integer.valueOf(arr[i]);
        }
        //for(String s: arr) {
            //int x = Integer.parseInt(s);
           // list.add(x);
        //}
        return list;
    }

    @JsonIgnore
    public String getCredentialsAsString() {
        ArrayList<Long> credentialList = new ArrayList<Long>();
        for(Credential c : Credentials) {
            credentialList.add(c.getId());
        }

        Collections.sort(credentialList);

        StringJoiner joiner = new StringJoiner(", ");
        for (Long id : credentialList) {
            joiner.add(id.toString());
        }
        String asString = joiner.toString();

        return asString;
    }
}
