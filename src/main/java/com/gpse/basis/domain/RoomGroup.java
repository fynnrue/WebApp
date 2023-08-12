package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.util.Collection;

@Entity
public class RoomGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.Public.class)
    private Long id;

    @Column
    @JsonView(Views.Public.class)
    private String name;

    @Column
    @JsonView(Views.Public.class)
    private Integer color;

    @JsonView(Views.Internal.class)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "roomGroup")
    private Collection<Room> rooms;

    @ManyToOne(fetch = FetchType.LAZY)
    private Floor floor;

    public RoomGroup(final String name, final Integer color, final Collection<Room> rooms) {
        this.name = name;
        this.color = color;
        this.rooms = rooms;
    }

    public RoomGroup() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    @JsonView(Views.Internal.class)
    public Collection<Room> getRooms() {
        return rooms;
    }

    public void setRooms(Collection<Room> rooms) {
        this.rooms = rooms;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public Floor getFloor() {
        return floor;
    }

    public Long getFloorId() {
        return floor.getId();
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }
}
