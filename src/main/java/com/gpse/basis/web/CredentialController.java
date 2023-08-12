package com.gpse.basis.web;

import com.gpse.basis.domain.SesamUser;
import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.credentials.CredentialGroupUnion;
import com.gpse.basis.domain.service.CredentialGroupService;
import com.gpse.basis.domain.service.CredentialService;
import com.gpse.basis.domain.service.UserService;
import com.gpse.basis.web.cmd.ChecklistCmd;
import com.gpse.basis.web.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * The CredentialController class is responsible for handling HTTP requests related to credentials.
 * Exposes various endpoints for retrieving and updating credentials.
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CredentialController {
    Logger logger = LoggerFactory.getLogger(CredentialController.class);
    private final CredentialService credentialService;
    private final CredentialGroupService credentialGroupService;

    private final UserService userService;

    /**
     * Constructor for the CredentialController class.
     *
     * @param credentialService      The credential service object.
     * @param userService           The user service object.
     * @param credentialGroupService The credential group service object.
     */
    @Autowired
    public CredentialController(
            final CredentialService credentialService,
            final UserService userService,
            final CredentialGroupService credentialGroupService) {

        this.credentialService = credentialService;
        this.credentialGroupService = credentialGroupService;
        this.userService = userService;
    }

    /**
     * Gets the credentials that can be issued by the current user.
     *
     * @param sesamUser the authenticated user requesting the issuable credentials
     * @return a collection of credentials that can be issued by the current user
     * @throws NotFoundException if the user requesting the credentials is not found
     */
    @GetMapping("/credentials/issuable")
    @Secured(SesamUser.ROLE_ISSUER)
    public Collection<Credential> getIssuableCredentials(@AuthenticationPrincipal final SesamUser sesamUser) {
        try {
            final var user = (SesamUser) userService.loadUserByUsername(sesamUser.getEmail());
            logger.debug("Retrieve issuable credentials for users: {}", user.getUsername());
            return user.getIssuableCredentials();
        } catch (UsernameNotFoundException exception) {
            logger.error("User not found");
            throw new NotFoundException();
        }
    }


    /**
     * Retrieves a collection of credentials.
     *
     * @return a collection of {@link Credential} objects representing the credentials
     */
    @GetMapping("/credentials")
    public Collection<Credential> getCredentials() {
        logger.debug("Request credentials");
        return Streamable.of(credentialService.getCredentials()).toList();
    }

    /**
     * Retrieves a Credential by its ID.
     *
     * @param id The ID of the Credential to retrieve.
     * @return The Credential with the specified ID.
     * @throws NotFoundException If no Credential with the specified ID is found.
     */
    @GetMapping("/credentials/{id}")
    public Credential getCredential(@PathVariable("id") final Long id) {
        logger.debug("Request credential with ID: {}", id);
        return credentialService.getCredentialById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Edits the checklist of a credential.
     * Checklist is used for the issuer to check off the requirements for the credential.
     *
     * @param id        The ID of the credential to edit the checklist for.
     * @param checklist The new checklist to set for the credential.
     * @return A message indicating that the credential has been edited.
     * @throws NotFoundException If the credential with the given ID is not found.
     */
    @PostMapping("/credentials/{id}/checklist")
    @Secured(SesamUser.ROLE_ADMIN)
    @Transactional
    public String editCredentialChecklist(@PathVariable("id") final Long id, @RequestBody final ChecklistCmd checklist) {
        final var credential = credentialService.getCredentialById(id).orElseThrow(NotFoundException::new);
        logger.info(Arrays.toString(checklist.getItems()));
        credential.setChecklist(List.of(checklist.getItems()));
        // credentialService.updateCredential(credential);
        return "Edited credential";
    }


    /**
     * Retrieves all credential group unions.
     * A credential group union is a combination of a credential and a credential group.
     *
     * @return a Collection of CredentialGroupUnion objects representing the union of credentials and credential groups
     */
    @GetMapping("/credentialGroupUnions")
    public Collection<CredentialGroupUnion> getAllCredentialGroupUnions() {
        logger.debug("Request all credential groups unions");
        return Stream.concat(
                Streamable.of(credentialService.getCredentials()).map(CredentialGroupUnion::fromCredential).stream(),
                Streamable.of(credentialGroupService.getCredentialGroups()).map(CredentialGroupUnion::fromCredentialGroup).stream()
        ).toList();
    }

    /**
     * Updates the credential with the given ID using the provided request data.
     *
     * @param id        The ID of the credential to update.
     * @param request   The web request containing the updated credential data.
     * @return A string indicating the success of the update operation.
     */
    @PostMapping("/admin/credentials/edit/{id}/save")
    public String updateCredential(@PathVariable("id") final Long id, final WebRequest request) {
        logger.debug("Update credential with ID: {}", id);
        final long credentialID = id;
        final String name = request.getParameter("name");
        final String origin = request.getParameter("origin");
        final String additional = request.getParameter("additional");
        Credential credential = getCredential(credentialID);
        credential.setName(name);
        credential.setOrigin(origin);
        credential.setAdditional(additional);
        this.credentialService.createCredential(credential);
        logger.debug("Successful update credential with ID: {}", id);
        return "success";
    }

}
