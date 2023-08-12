package com.gpse.basis.web.cmd;

/**
 * Cmd for creating room groups.
 */
public class RoomGroupCreation {
    /**
     * The name of the room group.
     */
    private String name;

    /**
     * The color of the room group.
     */
    private Integer color;

    public RoomGroupCreation(final String name, final Integer color) {
        this.name = name;
        this.color = color;
    }

    public RoomGroupCreation() {
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
}
