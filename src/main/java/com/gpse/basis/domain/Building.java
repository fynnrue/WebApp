package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gpse.basis.web.ImageController;
import jakarta.persistence.*;

import java.util.List;

/**
 * Entity which holds the relevant information for a building.
 */
@Entity
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String name;

    @Column
    private Long imageId;

    @JsonIgnore
    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    @OneToMany(cascade = CascadeType.ALL)
    private List<Floor> floors;

    protected Building() {
    }

    public Building(final String name, final Long imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public Long getImageId() {
        return imageId;
    }

    public String getImage() {
        return ImageController.urlFor(imageId);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setImageId(final Long imageId) {
        this.imageId = imageId;
    }

}
