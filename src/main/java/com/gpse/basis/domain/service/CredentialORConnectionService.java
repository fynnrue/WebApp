package com.gpse.basis.domain.service;

import com.gpse.basis.domain.credentials.CredentialORConnection;

/**
 * Service for storing CredentialORConnections.
 */
public interface CredentialORConnectionService {
    /**
     * Add a connection to the db.
     *
     * @param connection The connection to add.
     */
    public void save(CredentialORConnection connection);

    /**
     * Delete a connection from the db.
     *
     * @param id The id of the connection which should be removed.
     */
    public void deleteById(Long id);

    /**
     * Validates a connection.
     * @param connection The connection to validate.
     * @return True if the connection is valid, false otherwise.
     */
    boolean validate(CredentialORConnection connection);
}
