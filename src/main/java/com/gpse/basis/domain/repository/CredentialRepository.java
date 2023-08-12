package com.gpse.basis.domain.repository;

import com.gpse.basis.domain.credentials.Credential;
import org.springframework.data.repository.CrudRepository;

public interface CredentialRepository extends CrudRepository<Credential, Long> {
}
