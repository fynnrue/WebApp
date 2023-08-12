package com.gpse.basis.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gpse.basis.web.ImageController;
import jakarta.persistence.*;

@Entity
public class WebsiteConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "background", column = @Column(name = "dark_background")),
            @AttributeOverride(name = "primary", column = @Column(name = "dark_primary")),
            @AttributeOverride(name = "surface", column = @Column(name = "dark_surface")),
            @AttributeOverride(name = "accent", column = @Column(name = "dark_accent"))
    })
    private ColorScheme darkMode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "background", column = @Column(name = "light_background")),
            @AttributeOverride(name = "primary", column = @Column(name = "light_primary")),
            @AttributeOverride(name = "surface", column = @Column(name = "light_surface")),
            @AttributeOverride(name = "accent", column = @Column(name = "light_accent"))
    })
    private ColorScheme lightMode;

    @Column
    private Long logoImageId;

    @Column
    private Long favIconImageId;

    @Column(columnDefinition = "TEXT")
    private String imprint;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ColorScheme getDarkMode() {
        return darkMode;
    }

    public ColorScheme getLightMode() {
        return lightMode;
    }

    @JsonIgnore
    public Long getLogoImageId() {
        return logoImageId;
    }

    @JsonIgnore
    public Long getFavIconImageId() {
        return favIconImageId;
    }

    public String getLogoImage() {
        return ImageController.urlFor(logoImageId);
    }

    public String getFavIconImage() {
        return ImageController.urlFor(favIconImageId);
    }


    public String getImprint() {
        return imprint;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDarkMode(ColorScheme darkMode) {
        this.darkMode = darkMode;
    }

    public void setLightMode(ColorScheme lightMode) {
        this.lightMode = lightMode;
    }

    public void setLogoImageId(Long logoImageId) {
        this.logoImageId = logoImageId;
    }

    public void setFavIconImageId(Long favIconImageId) {
        this.favIconImageId = favIconImageId;
    }

    public void setImprint(String imprint) {
        this.imprint = imprint;
    }

    protected WebsiteConfiguration(){}

    public WebsiteConfiguration(
            final String name,
            final ColorScheme darkMode,
            final ColorScheme lightMode,
            final Long logoImageId,
            final Long favIconImageId,
            final String imprint) {

        this.name = name;
        this.darkMode = darkMode;
        this.lightMode = lightMode;
        this.logoImageId = logoImageId;
        this.favIconImageId = favIconImageId;
        this.imprint = imprint;
    }

}
