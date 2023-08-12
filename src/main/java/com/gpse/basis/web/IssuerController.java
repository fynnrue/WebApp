package com.gpse.basis.web;

import com.gpse.basis.domain.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The IssuerController class is a REST controller that handles issuing credentials and generating QR codes.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class IssuerController {

    private final Environment environment;
    private static final Logger log = LoggerFactory.getLogger(IssuerController.class);

    /**
     * Creates an instance of the IssuerController class.
     *
     * @param environment the environment object.
     */
    @Autowired
    public IssuerController(final Environment environment) {
        this.environment = environment;
    }

    /**
     * Retrieves the URL for generating a QR code for issuing a credential.
     *
     * @param request The WebRequest object containing the request parameters.
     * @return The URL for generating the QR code.
     */
    @PostMapping("/issuer/create/qr")
    public String getQRUrl(final WebRequest request) {
        final RestTemplate restTemplate = new RestTemplate();

        final var uri = environment.getProperty("issue-API.uri");
        final var username = environment.getProperty("issue-API.username");
        final var password = environment.getProperty("issue-API.password");


        final HttpHeaders headers = Utils.issueAPIHeaders(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);

        final JSONObject jsonObject = new JSONObject();
        final JSONObject credential = new JSONObject();
        final JSONArray attributes = new JSONArray();

        //Attributes:
        final List<String> names = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        try {
            for (Iterator<String> it = request.getParameterNames(); it.hasNext(); ) {
                String parameterKey = it.next();

                if (parameterKey.equals("credentialDefinitionId")) {
                    String did = request.getParameter("credentialDefinitionId");
                    credential.put("credentialDefinitionId", did);
                    assert did != null;
                    switch (did) {
                        case "$T-MEMBER", "$T-TRAINING" -> jsonObject.put("agent", "tlabs");
                        case "$U-MEMBER", "$U-TRAINING" -> jsonObject.put("agent", "university");
                    }
                } else {
                    names.add(parameterKey);
                    values.add(request.getParameter(parameterKey));
                }
            }


            for (int i = 0; i < names.size(); i++) {
                attributes.put(
                        new JSONObject().put("name", names.get(i))
                                .put("value", values.get(i))
                );
            }

            credential.put("attributes", attributes);
            jsonObject.put("credential", credential);
            log.info("JSON object created: {}", jsonObject);
        } catch (JSONException e) {
            log.error("JSON exception encountered: " + e.getMessage());
        }

        final HttpEntity<String> requestEntity = new HttpEntity<>(jsonObject.toString(), headers);

        try {
            final ResponseEntity<String> response = restTemplate.exchange(uri + "/api/credential/issue", HttpMethod.POST, requestEntity, String.class);
            log.debug("Response from issuer API: {}", response.getBody());
            log.info("API call successful, response received.");
            return response.getBody();
        } catch (RestClientException e) {
            log.error("RestClientException encountered: " + e.getMessage());
            return "error";
        }
    }
}
