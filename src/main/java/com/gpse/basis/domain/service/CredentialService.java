package com.gpse.basis.domain.service;

import com.gpse.basis.domain.credentials.Credential;

import java.util.Optional;

/**
 * Service for managing Credential Schemas
 */
public interface CredentialService {
    /**
     * Get all credentials in the db.
     *
     * @return All credentials stored in the db.
     */
    Iterable<Credential> getCredentials();

    /**
     * Get a credential with a specified id.
     *
     * @param id The id of the desired credential.
     * @return The Credential if it exists in the database.
     */
    Optional<Credential> getCredentialById(Long id);

    /**
     * Remove a credential from the database.
     *
     * @param id The id of the credential to delete.
     */
    void removeCredential(Long id);

    /**
     * Add a credential to the database.
     *
     * @param credential The credential to add to the database.
     */
    void createCredential(Credential credential);


    /**
     * Get the ID of a credential by giving its credentialDID
     *
     * @param credentialDID DefinitionID of credential
     *
     * @return ID of associated credential
     */
    Credential getCredentialID(String credentialDID);
}
