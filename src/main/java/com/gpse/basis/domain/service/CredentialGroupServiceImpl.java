package com.gpse.basis.domain.service;

import com.gpse.basis.domain.credentials.Credential;
import com.gpse.basis.domain.credentials.CredentialGroup;
import com.gpse.basis.domain.repository.CredentialGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CredentialGroupServiceImpl implements CredentialGroupService {
    private final CredentialGroupRepository credentialGroupRepository;

    @Autowired
    public CredentialGroupServiceImpl(CredentialGroupRepository repository) {
        this.credentialGroupRepository = repository;
    }

    @Override
    public List<CredentialGroup> getCredentialGroups() {
        final List<CredentialGroup> credentialGroups = new ArrayList<>();
        credentialGroupRepository.findAll().forEach(credentialGroups::add);
        return credentialGroups;
    }

    @Override
    public Optional<CredentialGroup> getCredentialGroupById(final Long id) {
        return credentialGroupRepository.findById(id);
    }

    @Override
    public CredentialGroup getCredentialGroupByName(final String name) {
        for (final CredentialGroup group : credentialGroupRepository.findAll()) {
            if(group.getName().equals(name)){
                return group;
            }
        }
        return null;
    }

    public CredentialGroup getCredentialGroupByCredentialIDs(final Collection <Credential> credentials) {
        for(final CredentialGroup group : credentialGroupRepository.findAll()) {
            var set1 = new HashSet<>(group.getCredentials());
            var set2 = new HashSet<>(credentials);

            boolean isEqual = set1.containsAll(set2) && set2.containsAll(set1);
            if(isEqual) {
                System.out.println("equal");
                return group;
            }
        }
        return null;
    }


    @Override
    public void removeCredentialGroup(final Long id) {
        credentialGroupRepository.deleteById(id);
    }


    public void deleteCredentialGroupByName(String name) {
        for (final CredentialGroup group : credentialGroupRepository.findAll()) {
            if(group.getName().equals(name)){
                removeCredentialGroup(group.getId());
            }
        }
    }
    @Override
    public String createCredentialGroup(CredentialGroup credentialGroup) {
        if(getCredentialGroupByName(credentialGroup.getName()) != null) {
            return "fail name exists";       //CredentialGroup already exists
        }
        if(getCredentialGroupByCredentialIDs(credentialGroup.getCredentials()) != null) {
            return "fail credentialIDs exist";
        }

        credentialGroupRepository.save(credentialGroup);

        return "success";
    }

    public String updateCredentialGroup (CredentialGroup credentialGroup, String oldName) {
        CredentialGroup oldGroup = getCredentialGroupByName(oldName);
        if(oldGroup != null) {
            if(oldGroup.getName().equals(credentialGroup.getName())) {
                if(oldGroup.getCredentialString().equals(credentialGroup.getCredentialString())) {
                    credentialGroupRepository.save(credentialGroup);
                    return "success";
                } else {        //different credentialIDs but same name
                    if(getCredentialGroupByCredentialIDs(credentialGroup.getCredentials()) != null) {
                        // a different credentialGroup already has these IDs
                        return "fail credentialIDs exist";
                    } else {
                        credentialGroupRepository.save(credentialGroup);
                        return "success";
                    }
                }
            } else {
                //different name
                if(getCredentialGroupByCredentialIDs(credentialGroup.getCredentials()) != null) {
                    return "fail credentialIDs exist";
                } else {
                    deleteCredentialGroupByName(oldName);   //delete old group
                    credentialGroupRepository.save(credentialGroup);
                    return "success";
                }
            }
        } else {
            return "fail name not found";
        }
    }
}