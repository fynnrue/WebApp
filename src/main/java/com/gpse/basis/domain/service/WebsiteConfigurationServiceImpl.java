package com.gpse.basis.domain.service;

import com.gpse.basis.domain.ColorScheme;
import com.gpse.basis.domain.WebsiteConfiguration;
import com.gpse.basis.domain.repository.WebsiteConfigurationRepository;
import com.gpse.basis.web.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Optional;

/*
 * This class is responsible for the websiteConfiguration management.
 */
@Service
public class WebsiteConfigurationServiceImpl implements WebsiteConfigurationService {

    private final WebsiteConfigurationRepository websiteConfigurationRepository;

    @Autowired
    public WebsiteConfigurationServiceImpl(WebsiteConfigurationRepository websiteConfigurationRepository) {
        this.websiteConfigurationRepository = websiteConfigurationRepository;
    }

    @Override
    public Optional<WebsiteConfiguration> getConfig() {
        var iterator = websiteConfigurationRepository.findAll().iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public WebsiteConfiguration addWebsiteConfiguration(WebsiteConfiguration websiteConfiguration) {
        return websiteConfigurationRepository.save(websiteConfiguration);
    }

    @Override
    public WebsiteConfiguration updateConfig(
            Long id, String name, ColorScheme darkMode,
            ColorScheme lightMode, Long logoImageId,
            Long favIconImageId, String imprint) {
        final WebsiteConfiguration websiteConfiguration = websiteConfigurationRepository.findById(id).orElseThrow(NotFoundException::new);
        websiteConfiguration.setName(name);
        websiteConfiguration.setDarkMode(darkMode);
        websiteConfiguration.setLightMode(lightMode);
        websiteConfiguration.setLogoImageId(logoImageId);
        websiteConfiguration.setFavIconImageId(favIconImageId);
        websiteConfiguration.setImprint(imprint);
        websiteConfigurationRepository.save(websiteConfiguration);
        return websiteConfiguration;
    }
}
