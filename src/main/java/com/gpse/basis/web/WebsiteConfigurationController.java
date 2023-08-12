package com.gpse.basis.web;

import com.gpse.basis.domain.ColorScheme;
import com.gpse.basis.domain.Image;
import com.gpse.basis.domain.WebsiteConfiguration;
import com.gpse.basis.domain.service.ImageService;
import com.gpse.basis.domain.service.WebsiteConfigurationService;
import com.gpse.basis.web.exceptions.BadRequestException;
import com.gpse.basis.web.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

/**
 * REST controller for managing website configuration.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class WebsiteConfigurationController {

    @Value("${app.max-image-size}")
    private long MAX_IMAGE_SIZE; // 10 MiB by default in application.properties
    private final WebsiteConfigurationService websiteConfigurationService;
    private final ImageService imageService;
    private static final Logger log = LoggerFactory.getLogger(WebsiteConfigurationController.class);

    @Autowired
    public WebsiteConfigurationController(final WebsiteConfigurationService websiteConfigurationService,
                                          final ImageService imageService) {
        this.websiteConfigurationService = websiteConfigurationService;
        this.imageService = imageService;
    }

    /**
     * Retrieves the website configuration.
     *
     * @return the WebsiteConfiguration object if it exists, otherwise throws a NotFoundException
     * @throws NotFoundException if the website configuration does not exist
     */
    @GetMapping("/admin/designsettings")
    @ResponseStatus(code = HttpStatus.OK)
    public WebsiteConfiguration getWebsiteConfiguration() {
        log.debug("Request website configuration");
        return websiteConfigurationService.getConfig().orElseThrow(NotFoundException::new);
    }

    /**
     * Stores the website configuration settings.
     * <p>
     * This method is annotated with @Transactional, @PostMapping, @ResponseStatus, and @Secured annotations
     * to handle the transaction, HTTP POST request, response status, and role-based security, respectively.
     *
     * @param name The name of the website configuration.
     * @param darkMode The color scheme for dark mode.
     * @param lightMode The color scheme for light mode.
     * @param logoImage The logo image file.
     * @param favIconImage The favicon image file.
     * @param imprint The imprint information.
     * @return The updated website configuration.
     * @throws BadRequestException if the logo or favicon image size exceeds the maximum image size.
     * @throws NotFoundException if the website configuration settings are not found.
     * @throws SQLException if an SQL exception occurs while storing the website configuration.
     */
    @Transactional
    @PostMapping("/admin/designsettings")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Secured("ROLE_EDITOR")
    public WebsiteConfiguration store(
            @RequestPart final String name,
            @RequestPart final ColorScheme darkMode,
            @RequestPart final ColorScheme lightMode,
            @RequestPart(required = false) final MultipartFile logoImage,
            @RequestPart(required = false) final MultipartFile favIconImage,
            @RequestPart final String imprint) throws SQLException {
        try {
            WebsiteConfiguration websiteConfiguration =
                    websiteConfigurationService.getConfig().orElseThrow(NotFoundException::new);
            websiteConfiguration.setName(name);
            websiteConfiguration.setDarkMode(darkMode);
            websiteConfiguration.setLightMode(lightMode);
            websiteConfiguration.setImprint(imprint);
            if(logoImage != null) {
                if(logoImage.getSize() > MAX_IMAGE_SIZE) {
                    log.error("Logo image size exceeds the limit");
                    throw new BadRequestException();
                }
                final Image logoIcon = imageService.addImage(logoImage);
                websiteConfiguration.setLogoImageId(logoIcon.getId());
            }
            if(favIconImage != null) {
                if(favIconImage.getSize() > MAX_IMAGE_SIZE) {
                    log.error("Favicon image size exceeds the limit");
                    throw new BadRequestException();
                }
                final Image favIcon = imageService.addImage(favIconImage);
                websiteConfiguration.setFavIconImageId(favIcon.getId());
            }
            log.debug("Website configuration updated");
            return websiteConfiguration;
        } catch (IOException e) {
            log.error("Error while updating the website configuration: {}", e.getMessage());
            throw new BadRequestException();
        }
    }


    /**
     * Updates the website configuration with the given parameters.
     *
     * @param id            The ID of the website configuration to update.
     * @param name          The new name for the website configuration.
     * @param darkMode      The new color scheme for dark mode.
     * @param lightMode     The new color scheme for light mode.
     * @param logoImage     The new logo image file.
     * @param favIconImage  The new favicon image file.
     * @param imprint       The new imprint string.
     * @return The updated website configuration.
     * @throws SQLException             If there is an error updating the configuration in the database.
     * @throws BadRequestException      If there is an error adding the images to the image service.
     */
    @PutMapping("/admin/designsettings/{id}")
    @Secured("ROLE_EDITOR")
    public WebsiteConfiguration update(
            @PathVariable("id") final Long id,
            @RequestPart final String name,
            @RequestPart final ColorScheme darkMode,
            @RequestPart final ColorScheme lightMode,
            @RequestPart final MultipartFile logoImage,
            @RequestPart final MultipartFile favIconImage,
            @RequestPart final String imprint) throws SQLException {
        final Image favIconIm;
        final Image logoIm;

        try {
            favIconIm = imageService.addImage(favIconImage);
            logoIm = imageService.addImage(logoImage);
            log.info("Website configuration successfully updated");
        } catch (IOException e) {
            log.error("Error while configurate website: {}", e.getMessage());
            throw new BadRequestException();
        }
        log.debug("Update website configuration with ID: {}", id);
        return websiteConfigurationService.updateConfig(
                id,
                name,
                darkMode,
                lightMode,
                logoIm.getId(),
                favIconIm.getId(),
                imprint);
    }

}
