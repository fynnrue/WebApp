package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Door {
    @Id
    @GeneratedValue
    @JsonView(Views.Public.class)
    private Long id;


    @Column
    @JsonView(Views.Public.class)
    private String apiId;

    @Column
    @JsonView(Views.Public.class)
    private String name;

    public Door() {
    }

    public Door(String apiId, String name) {
        this.apiId = apiId;
        this.name = name;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
