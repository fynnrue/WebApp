package com.gpse.basis.domain.service;

import com.gpse.basis.domain.SesamUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

/*
 * This interface is used to define the methods that are used in the UserServiceImpl class.
 */
public interface UserService extends UserDetailsService {
    List<SesamUser> showUsers();

    void createUser(String email, String password, String firstName, String lastName, String... roles);

    boolean emailExists(String email);

    String createUnregisteredUser(String email, String password, String forename,
                                String surname, String... roles);

    boolean registerUser(String email, String token);

    void updateUser(SesamUser user);

    List<SesamUser> getUsersFiltered(String permissions, String activated, String searchType, String search);

    boolean activateUsers(String emails);

    boolean deactivateUsers(String emails);

    boolean deleteUsers(String emails);

    List<SesamUser> showUnregisteredUsers();

    List<SesamUser> showAllUsers();

    Optional<SesamUser> getUserByEmail(String email);

    String generatePasswordResetToken(String email);

    boolean resetPassword(String email, String password, String token);
}
