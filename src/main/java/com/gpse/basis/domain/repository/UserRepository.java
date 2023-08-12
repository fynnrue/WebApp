package com.gpse.basis.domain.repository;

import com.gpse.basis.domain.SesamUser;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<SesamUser, String> {
}
