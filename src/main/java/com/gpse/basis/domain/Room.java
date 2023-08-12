package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.credentials.CredentialGroupUnion;
import com.gpse.basis.domain.credentials.CredentialORConnection;
import com.gpse.basis.web.cmd.ORConnectionCmd;
import com.gpse.basis.web.cmd.RoomGroupInfo;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a room in a building.
 */
@Entity
public class Room {
    @Id
    @GeneratedValue
    @JsonView(Views.Public.class)
    private Long id;

    @Column
    @JsonView(Views.Public.class)
    private String name;

    @Column
    @JsonView(Views.Public.class)
    private String description;

    @ElementCollection
    @JsonView(Views.Public.class)
    private List<Position> vertices;

    @OneToMany
    @JsonIgnore
    private List<CredentialORConnection> orConnections;

    @ElementCollection
    @JsonView(Views.Public.class)
    private List<String> doors;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RoomGroup roomGroup;

    public Room() {
    }

    public Room(
            final String name,
            final String description,
            final List<Position> vertices,
            final List<CredentialORConnection> orConnections,
            final List<String> doors
    ) {
        this.doors = doors;
        this.name = name;
        this.description = description;
        this.vertices = vertices;
        this.orConnections = orConnections;
    }

    public List<String> getDoors() {
        return doors;
    }

    public void setDoors(List<String> doors) {
        this.doors = doors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Position> getVertices() {
        return vertices;
    }

    public void setVertices(List<Position> vertices) {
        this.vertices = vertices;
    }

    @JsonIgnore
    public List<CredentialORConnection> getOrConnections() {
        return orConnections;
    }

    public void setOrConnections(List<CredentialORConnection> orConnections) {
        this.orConnections = orConnections;
    }

    @JsonView(Views.Public.class)
    public Collection<ORConnectionCmd> getRequiredCredentials() {
        return orConnections.stream().map(c -> new ORConnectionCmd(
                Stream.concat(
                        c.getGroups().stream().map(CredentialGroupUnion::fromCredentialGroup),
                        c.getCredentials().stream().map(CredentialGroupUnion::fromCredential)
                ).collect(Collectors.toList()),
                c.getRequirements()
        )).collect(Collectors.toList());
    }

    @JsonIgnore
    public RoomGroup getRoomGroup() {
        return roomGroup;
    }

    @JsonView(Views.Public.class)
    public Long getFloorId() {
        return roomGroup.getFloor().getId();
    }

    @JsonView(Views.Public.class)
    public RoomGroupInfo getGroupInfo() {
        return new RoomGroupInfo(roomGroup.getName(), roomGroup.getColor());
    }

    public void setRoomGroup(RoomGroup roomGroup) {
        this.roomGroup = roomGroup;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
