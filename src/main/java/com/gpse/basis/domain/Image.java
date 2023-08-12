package com.gpse.basis.domain;

import jakarta.persistence.*;

import java.sql.Blob;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Lob
    private Blob data;

    @Column
    private String type;

    protected Image() {
    }

    public Image(Blob data, String type) {
        this.data = data;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Blob getData() {
        return data;
    }

    public void setData(Blob data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
