package com.gpse.basis.domain.service;

import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository repository;

    @Autowired
    public CredentialServiceImpl(CredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public Iterable<Credential> getCredentials() {
        return repository.findAll();
    }

    @Override
    public Optional<Credential> getCredentialById(final Long id) {
        return repository.findById(id);
    }

    public Credential getCredentialID(final String credentialDID) {
        for (final Credential credential : repository.findAll()) {
            if(credential.getCredentialDid().equals(credentialDID)) {
                return credential;
            }
        }
        return null;
    }

    @Override
    public void removeCredential(final Long id) {
        repository.deleteById(id);
    }

    @Override
    public void createCredential(Credential credential) {
        repository.save(credential);
    }

}
