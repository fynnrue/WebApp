package com.gpse.basis.web;

import com.gpse.basis.domain.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class DoorController {

    private final Environment environment;

    private static final Logger log = LoggerFactory.getLogger(DoorController.class);

    @Autowired
    public DoorController(final Environment environment) {
        this.environment = environment;
    }

    /**
     * Get all the doors from the Issuer-API
     * Currently: You need to be logged into the University Network to access the API.
     *
     * @return The doors as a json encoded list.
     */
    @GetMapping("/doors")
    public ResponseEntity<String> allDoors() {
        final var restTemplate = new RestTemplate();

        final var uri = environment.getProperty("issue-API.uri");
        final var username = environment.getProperty("issue-API.username");
        final var password = environment.getProperty("issue-API.password");
        log.debug("Retrieve all doors");
        final var requestEntity = new HttpEntity<List<String>>(null, Utils.issueAPIHeaders(username, password));
        final ResponseEntity<String> response = restTemplate.exchange(
                uri + "/api/proof/config",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        log.info("API request sent to retrieve the door configuration");

        final var headers = new HttpHeaders();
        // Set Content-Type manually since the api returns the wrong content type.
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response.getBody(), headers, HttpStatusCode.valueOf(200));
    }
}
