package com.gpse.basis.domain.service;

import com.gpse.basis.domain.ColorScheme;
import com.gpse.basis.domain.WebsiteConfiguration;

import java.util.Optional;

/**
 * Interface that defines the methods that are used in the WebsiteConfigurationServiceImpl class.
 */
public interface WebsiteConfigurationService {
    /**
     * Find a website configuration.
     * @return The website configuration if it exists
     */
    Optional<WebsiteConfiguration> getConfig();



    /**
     * Add a website configuration.
     *
     * @param websiteConfiguration The new websiteConfiguration.
     * @return The created websiteConfiguration
     */
    WebsiteConfiguration addWebsiteConfiguration(WebsiteConfiguration websiteConfiguration);

    /**
     * Update the existing website config.
     * @param id                    The id of the Config
     * @param name                  The updated name for the website
     * @param darkMode              The updated darkMode colorScheme
     * @param lightMode             The updated lightMode colorScheme
     * @param logoImageId           The updated image ID of the logo
     * @param favIconImageId        The updated image ID of the fav icon
     * @param imprint               The updated imprint
     *
     * @return The updated websiteConfiguration.
     */
    WebsiteConfiguration updateConfig(Long id, String name, ColorScheme darkMode,
                                      ColorScheme lightMode, Long logoImageId,
                                      Long favIconImageId, String imprint);
}
