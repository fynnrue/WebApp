package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Embeddable;

@Embeddable
@JsonView(Views.Public.class)
public class Position {
    private double x;
    private double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position(double[] pos) {
        if (pos.length != 2) {
            throw new IllegalArgumentException("Position must be of length 2");
        }
        this.x = pos[0];
        this.y = pos[1];
    }

    public Position() {
    }

    public double[] getPositionAsArray() {
        return new double[]{x, y};
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
