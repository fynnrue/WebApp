package com.gpse.basis.domain.service;

import com.gpse.basis.domain.credentials.CredentialGroupUnion;
import com.gpse.basis.domain.credentials.CredentialORConnection;
import com.gpse.basis.domain.credentials.PredicateValueType;
import com.gpse.basis.domain.repository.CredentialORConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class CredentialORConnectionServiceImpl implements CredentialORConnectionService {

    private final CredentialORConnectionRepository repository;

    @Autowired
    public CredentialORConnectionServiceImpl(final CredentialORConnectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(CredentialORConnection connection) {
        repository.save(connection);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean validate(CredentialORConnection connection) {
        final var attributes = new HashSet<String>();
        final var allCredentials = Stream.concat(
                connection.getCredentials().stream().map(CredentialGroupUnion::fromCredential),
                connection.getGroups().stream().map(CredentialGroupUnion::fromCredentialGroup)
        ).toList();
        allCredentials.stream().findFirst().ifPresent(
                credential -> attributes.addAll(credential.getAttributes())
        );
        for (var credential : allCredentials) {
            attributes.retainAll(credential.getAttributes());
        }
        System.out.println(attributes);

        for (var requirement : Optional.ofNullable(connection.getRequirements()).orElse(List.of())) {
            if (!attributes.contains(requirement.getAttributeName())) return false;
            if (requirement.getValueType() != PredicateValueType.NUMBER) {
                // If the value is a string, it must be “$TODAY-YYYYMMDD"
                if (requirement.getValueType() == PredicateValueType.DATE) {
                    if (!requirement.getAttributeValue().matches("\\d{4}-\\d{2}-\\d{2}")) return false;
                } else if (!requirement.getAttributeValue().equals("$TODAY-YYYYMMDD")) {
                    return false;
                }
            }
        }

        final var attributeRequirements = connection.getAttributeRequirements();
        return attributeRequirements == null || attributes.contains(attributeRequirements.getAttributeName());
    }
}
