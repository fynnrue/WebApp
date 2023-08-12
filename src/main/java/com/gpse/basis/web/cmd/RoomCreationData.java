package com.gpse.basis.web.cmd;

import com.gpse.basis.domain.Position;

import java.util.List;

/**
 *
 */
public class RoomCreationData {
    private String name;
    private String description;

    private List<Position> vertices;

    public RoomCreationData(final String name, final String description, final List<Position> vertices) {
        this.name = name;
        this.description = description;
        this.vertices = vertices;
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
}
