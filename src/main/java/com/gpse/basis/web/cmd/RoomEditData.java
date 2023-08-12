package com.gpse.basis.web.cmd;

import com.gpse.basis.domain.Position;

import java.util.List;

/**
 * Cmd for editing room information.
 */
public class RoomEditData {
    /**
     * The new name of the room. This field is optional.
     */
    private String name;

    /**
     * The new description of the room. This field is optional.
     */
    private String description;

    /**
     * The new vertices of the room. This field is optional.
     */
    private List<Position> vertices;

    /**
     * The new doors of the room. This field is optional.
     */
    private List<String> doors;

    /**
     * The new required credentials of the room. This field is optional.
     */
    private List<ORConnectionCmd> requiredCredentials;

    private Long roomGroupId;

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

    public List<String> getDoors() {
        return doors;
    }

    public void setDoors(List<String> doors) {
        this.doors = doors;
    }

    public List<ORConnectionCmd> getRequiredCredentials() {
        return requiredCredentials;
    }

    public void setRequiredCredentials(List<ORConnectionCmd> requiredCredentials) {
        this.requiredCredentials = requiredCredentials;
    }

    public Long getRoomGroupId() {
        return roomGroupId;
    }

    public void setRoomGroupId(Long roomGroupId) {
        this.roomGroupId = roomGroupId;
    }
}
