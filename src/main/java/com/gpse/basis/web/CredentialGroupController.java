package com.gpse.basis.web;

import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.credentials.CredentialGroup;
import com.gpse.basis.domain.service.CredentialGroupService;
import com.gpse.basis.domain.service.CredentialService;
import com.gpse.basis.web.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing credential groups.
 */
@RestController
@CrossOrigin
@RequestMapping("/api")
public class CredentialGroupController {

    private final CredentialGroupService credentialGroupService;
    private final CredentialService credentialService;
    private static final Logger log = LoggerFactory.getLogger(BuildingController.class);

    /**
     * Constructs a new CredentialGroupController with the specified CredentialGroupService and CredentialService.
     *
     * @param credentialGroupService the CredentialGroupService to be used by the controller
     * @param credentialService the CredentialService to be used by the controller
     */
    public CredentialGroupController(final CredentialGroupService credentialGroupService, CredentialService credentialService) {
        this.credentialGroupService = credentialGroupService;
        this.credentialService = credentialService;
    }


    /**
     * Retrieves a list of all credential groups.
     *
     * @return List of CredentialGroup objects representing all credential groups.
     */
    @GetMapping("/admin/credentialgroups/all")
    public List<CredentialGroup> getCredentialGroups() {
        log.debug("Request credential groups");
        return credentialGroupService.getCredentialGroups();
    }

    /**
     * Adds a new credential group.
     *
     * @param request the web request containing the parameters needed to create the credential group
     *                - CredentialIDs: a comma-separated string of credential IDs
     *                - name: the name of the credential group
     *                - origin: the origin of the credential group
     *                - additional: additional information about the credential group
     * @return a string representation of the created credential group
     * @throws NotFoundException if CredentialIDs is null
     */
    @PostMapping("/credentialgroups/add")
    public String addCredentialGroup(final WebRequest request) {
        final String credentialIDs = request.getParameter("CredentialIDs");
        if (credentialIDs == null) {
            log.error("CredentialIDs is null");
            throw new NotFoundException("CredentialIDs is null");
        }
        log.debug("Add new credentials group");
        final String name = request.getParameter("name");
        final String origin = request.getParameter("origin");
        final String additional = request.getParameter("additional");
        final Collection <Credential> credentials = getCredentialsFromString(credentialIDs);

        CredentialGroup credentialGroup = new CredentialGroup(credentials, name,
                origin, additional);

        credentialGroup.setCredentialString(credentialGroup.getCredentialsAsString());
        log.debug("Add new credentials group successfully");
        return this.credentialGroupService.createCredentialGroup(credentialGroup);
    }

    /**
     * Updates a credential group with new parameters.
     *
     * @param request the WebRequest object containing the request parameters
     * @return a String indicating the result of the update operation
     * @throws NotFoundException if the CredentialIDs parameter is null
     */
    @PostMapping("/admin/credentialgroups/update")
    public String updateCredentialGroup(final WebRequest request) {
        final String credentialIDs = request.getParameter("CredentialIDs");
        if (credentialIDs == null) {
            log.error("CredentialIDs is null");
            throw new NotFoundException("CredentialIDs is null");
        }
        log.debug("Update new credentials group");
        final String name = request.getParameter("name");
        final String origin = request.getParameter("origin");
        final String additional = request.getParameter("additional");
        final String oldGroupName = request.getParameter("oldGroupName");
        final Collection <Credential> credentials = getCredentialsFromString(credentialIDs);

        CredentialGroup credentialGroup = this.credentialGroupService.getCredentialGroupByName(oldGroupName);
        credentialGroup.setCredentials(credentials);
        credentialGroup.setCredentialString(credentialIDs);
        credentialGroup.setName(name);
        credentialGroup.setOrigin(origin);
        credentialGroup.setAdditional(additional);
        log.debug("Update new credentials group successfully");
        return this.credentialGroupService.updateCredentialGroup(credentialGroup, oldGroupName);
    }

    /**
     * Deletes a credential group based on the name provided in the request.
     *
     * @param request The web request containing the name of the credential group to be deleted.
     */
    @PostMapping("/admin/credentialgroups/delete")
    public void deleteCredentialGroup(final WebRequest request) {
        log.debug("Request delete credential group");
        this.credentialGroupService.deleteCredentialGroupByName(request.getParameter("name"));
    }

    /**
     * Retrieves a CredentialGroup by name from the server.
     *
     * @param request the WebRequest object containing the HTTP request information.
     * @return the CredentialGroup object retrieved from the server or null if not found.
     */
    @GetMapping("/admin/credentialgroups/get")
    public CredentialGroup getCredentialGroup(final WebRequest request) {
        log.debug("Request credential group");
        final String name = request.getParameter("name");
        return credentialGroupService.getCredentialGroupByName(name);
    }

    /**
     * Retrieves a list of credentials from a string of credential IDs.
     *
     * @param credentialIDs the string of credential IDs separated by ", "
     * @return a list of credentials matching the provided IDs
     */
    public List <Credential> getCredentialsFromString(final String credentialIDs) {
        log.debug("Extract credentials from string: {}", credentialIDs);
        List <Credential> list = new ArrayList<>();
        String[] arr = credentialIDs.split(", ");
        for (String s : arr) {
            long num = Integer.parseInt(s);
            Optional<Credential> credential = credentialService.getCredentialById(num);
            credential.ifPresent(list::add);
        }
        return list;
    }
}
