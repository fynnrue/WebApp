package com.gpse.basis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ColorScheme {
    @Column
    String background;

    @Column
    String primary;

    @Column
    String surface;

    @Column
    String accent;

    public ColorScheme() {}

    public ColorScheme(String background, String primary, String surface, String accent) {
        this.background = background;
        this.primary = primary;
        this.surface = surface;
        this.accent = accent;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getAccent() {
        return accent;
    }
}
