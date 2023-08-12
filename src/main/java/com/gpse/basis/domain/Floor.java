package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.web.ImageController;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.List;

/**
 * Entity which holds the relevant information for a floor.
 */
@Entity
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    @JsonView(Views.Public.class)
    private Long id;

    @Column
    @JsonView(Views.Public.class)
    private String name;

    @Column
    @JsonView(Views.Internal.class)
    private Long imageId;

    // First index is the default group.
    @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE, mappedBy = "floor")
    @JsonView(Views.Internal.class)
    private Collection<RoomGroup> roomGroups;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Building building;

    protected Floor() {
    }

    public Floor(final String name, final Long imageId, final RoomGroup defaultGroup) {
        this.name = name;
        this.imageId = imageId;
        this.roomGroups = List.of(defaultGroup);
    }

    public Collection<RoomGroup> getRoomGroups() {
        return roomGroups;
    }

    public void setRoomGroups(Collection<RoomGroup> rooms) {
        this.roomGroups = rooms;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @JsonView(Views.Internal.class)
    public Long getImageId() {
        return imageId;
    }

    @JsonView(Views.Public.class)
    public String getImage() {
        return ImageController.urlFor(imageId);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setImageId(final Long imageId) {
        this.imageId = imageId;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    @JsonView(Views.Public.class)
    public RoomGroup getDefaultRoomGroup() {
        return roomGroups.stream().findFirst().orElseThrow(() -> new RuntimeException("No default room group found"));
    }

    @JsonView(Views.Public.class)
    public String buildingName() {
        return building.getName();
    }
}
