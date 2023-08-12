package com.gpse.basis.web.cmd;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Views;

public class RoomGroupInfo {
    private String name;
    private Integer color;

    public RoomGroupInfo() {
    }

    public RoomGroupInfo(final String name, final Integer color) {
        this.name = name;
        this.color = color;
    }

    @JsonView(Views.Public.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @JsonView(Views.Public.class)
    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }
}
