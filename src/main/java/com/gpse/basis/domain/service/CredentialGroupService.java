package com.gpse.basis.domain.service;

import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.credentials.CredentialGroup;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Service for managing Credential Group Schemes
 */

public interface CredentialGroupService {
    /**
     * Get all credentials in the db.
     *
     * @return All credentials stored in the db.
     */
    List<CredentialGroup> getCredentialGroups();

    /**
     * Get a credential with a specified id.
     *
     * @param id The id of the desired credential.
     * @return The Credential if it exists in the database.
     */
    Optional<CredentialGroup> getCredentialGroupById(Long id);

    /**
     * Remove a credentialGroup from the database.
     *
     * @param id The id of the credentialGroup to delete.
     */
    void removeCredentialGroup(Long id);

    /**
     * Add a credentialGroup to the database.
     *
     * @param credentialGroup The credentialGroup to add to the database.
     * @return String that contains whether credentialGroup didnt exist before
     */
    String createCredentialGroup(CredentialGroup credentialGroup);

    /**
     * Get credentialGroup with given name from the database
     *
     * @param name of the CredentialGroup
     * @return CredentialGroup from the database
     */
    CredentialGroup getCredentialGroupByName(String name);

    /**
     *
     * @param credentials
     * @return CredentialGroup that contains given credentialIDs
     */

    CredentialGroup getCredentialGroupByCredentialIDs(Collection<Credential> credentials);

    /**
     * Delete credentialGroup with given name form the database
     *
     * @param name of the credentialGroup that should be deleted
     */
    void deleteCredentialGroupByName(String name);

    String updateCredentialGroup(CredentialGroup credentialGroup, String oldName);
}
