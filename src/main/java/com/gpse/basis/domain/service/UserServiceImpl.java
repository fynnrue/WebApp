package com.gpse.basis.domain.service;

import com.gpse.basis.domain.SesamUser;
import com.gpse.basis.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * This class is responsible for the user management.
 * It implements the UserService interface.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<SesamUser> showUsers() {
        final List<SesamUser> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            if (user.isRegistered()) {
                users.add(user);
            }
        });
        return users;
    }

    @Override
    public List<SesamUser> showUnregisteredUsers() {
        final List<SesamUser> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            if (!user.isRegistered()) {
                users.add(user);
            }
        });
        return users;
    }

    @Override
    public List<SesamUser> showAllUsers() {
        final List<SesamUser> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            users.add(user);
        });
        return users;
    }

    @Override
    public Optional<SesamUser> getUserByEmail(final String email) {
        Optional<SesamUser> optionalSesamUser = Optional.empty();
        for (SesamUser user : userRepository.findAll()){
            if (user.getEmail().equals(email) && user.isRegistered()){
                optionalSesamUser = Optional.of(user);
                break;
            }
        }
        return optionalSesamUser;
    }

    @Override
    public void createUser(final String email, final String password, final String forename,
                           final String surname, final String... roles) {
        final SesamUser user = new SesamUser(forename, surname, email, password);
        user.setRegistered(true);
        user.setActivated(true);
        for (final String role : roles) {
            if(!role.equals("")){
                user.addRole(role);
            }
        }
        this.userRepository.save(user);
    }

    @Override
    public String createUnregisteredUser(final String email, final String password, final String forename,
                                         final String surname, final String... roles) {
        SesamUser user;
        if (emailExists(email)) {
            user = loadUserByUsername(email);

            if (!user.isRegistered()) {
                user.setForename(forename);
                user.setSurname(surname);
                user.setPassword(password);

                for (final String role : roles) {
                    if(!role.equals("")){
                        user.addRole(role);
                    }
                }
                String token = user.generateRegistrationToken();
                updateUser(user);
                return token;
            } else {
                return "";
            }
        } else {
            user = new SesamUser(forename, surname, email, password);
            for (final String role : roles) {
                if(!role.equals("")){
                    user.addRole(role);
                }
            }
        }

        String token = user.generateRegistrationToken();
        this.userRepository.save(user);
        return token;
    }

    @Override
    public boolean registerUser(String email, String token) {
        SesamUser user;

        if (emailExists(email)) {
            user = loadUserByUsername(email);
            if (user.isRegistered()) {
                return true;
            } else {
                if (user.isRegistrationTokenValid(token)) {
                    user.setRegistered(true);
                    updateUser(user);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void updateUser(final SesamUser user) {
        userRepository.deleteById(user.getEmail());
        userRepository.save(user);
    }

    @Override
    public boolean emailExists(final String email) {
        final List<SesamUser> sesamUsers = this.showAllUsers();

        for (final SesamUser user : sesamUsers) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SesamUser loadUserByUsername(final String email) throws UsernameNotFoundException {
        return userRepository.findById(email)
                .orElseThrow(() -> new UsernameNotFoundException("SesamUser " + email + " not found."));
    }

    @Override
    public List<SesamUser> getUsersFiltered(String permissions, String activated, String searchType, String search) {
        // First get all users from database then search which follow restrictions.
        List<SesamUser> users = showUsers();
        Iterator<SesamUser> iterator = users.iterator();

        // More efficient to run over the elements with the iterator than just the normal list.
        while (iterator.hasNext()) {
            boolean removed = false;
            SesamUser user = iterator.next();

            // Check if user satisfies permission restriction.
            switch (permissions) {
                case (""):
                    break;
                case ("admin"):
                    if (!user.getRoles().contains("ROLE_ADMIN")) {
                        removed = true;
                        iterator.remove();
                    }
                    break;
                case ("issuer"):
                    if (!user.getRoles().contains("ROLE_ISSUER")) {
                        removed = true;
                        iterator.remove();
                    }
                    break;
                case ("planner"):
                    if (!user.getRoles().contains("ROLE_EDITOR")) {
                        removed = true;
                        iterator.remove();
                    }
                    break;
                case ("none"):
                    if (!(user.getRoles().size() == 0)) {
                        removed = true;
                        iterator.remove();
                    }
                    break;
            }

            if (!removed) {
                // Check if user satisfies account activation restriction.
                switch (activated) {
                    case (""):
                        break;
                    case ("yes"):
                        if (!user.isActivated()) {
                            removed = true;
                            iterator.remove();
                        }
                        break;
                    case ("no"):
                        if (user.isActivated()) {
                            removed = true;
                            iterator.remove();
                        }
                        break;
                }
            }

            if (!removed) {
                // Check if user satisfies search in chosen user attribute restriction.
                if (!search.equals("")) {
                    switch (searchType) {
                        case (""):
                            break;
                        case ("forename"):
                            if (!user.getForename().toLowerCase().contains(search.toLowerCase())) {
                                iterator.remove();
                            }
                            break;
                        case ("surname"):
                            if (!user.getSurname().toLowerCase().contains(search.toLowerCase())) {
                                iterator.remove();
                            }
                            break;
                        case ("email"):
                            if (!user.getEmail().toLowerCase().contains(search.toLowerCase())) {
                                iterator.remove();
                            }
                            break;
                    }
                }
            }
        }
        return users;
    }

    @Override
    public boolean activateUsers(String emails) {
        String[] emailsArr = emails.split(", ");
        boolean successfull = true;

        for (String email : emailsArr) {
            SesamUser user;

            try {
                user = loadUserByUsername(email);
            } catch (Exception e) {
                user = null;
            }

            if (user != null) {
                user.setActivated(true);
                userRepository.save(user);
            } else {
                successfull = false;
            }
        }

        return successfull;
    }

    @Override
    public boolean deactivateUsers(String emails) {
        String[] emailsArr = emails.split(", ");
        boolean successfull = true;

        for (String email : emailsArr) {
            SesamUser user;

            try {
                user = loadUserByUsername(email);
            } catch (Exception e) {
                user = null;
            }

            if (user != null) {
                user.setActivated(false);
                userRepository.save(user);
            } else {
                successfull = false;
            }
        }

        return successfull;
    }

    @Override
    public boolean deleteUsers(String emails) {
        String[] emailsArr = emails.split(", ");
        boolean successfull = true;

        for (String email : emailsArr) {
            SesamUser user;

            try {
                user = loadUserByUsername(email);
            } catch (Exception e) {
                user = null;
            }

            if (user != null) {
                userRepository.delete(user);
            } else {
                successfull = false;
            }
        }

        return successfull;
    }

    @Override
    public String generatePasswordResetToken(String email) {
        SesamUser user = loadUserByUsername(email);
        if (user == null) {
            return null;
        }
        String token = user.generatePasswordResetToken();
        userRepository.save(user);
        return token;
    }

    @Override
    public boolean resetPassword(String email, String password, String token) {
        SesamUser user = loadUserByUsername(email);
        if (!user.isResetTokenValid(token)) {
            return false;
        }
        if (password.equals("")) {
            return false;
        }
        user.setPassword(password);
        userRepository.save(user);
        return true;
    }
}
