package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gpse.basis.domain.credentials.Credential;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;


@Entity
public class SesamUser implements UserDetails {

    public static final String ROLE_EDITOR = "ROLE_EDITOR";
    public static final String ROLE_ISSUER = "ROLE_ISSUER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Serial
    private static final long serialVersionUID = 215L;

    @Column(unique = true)
    private String username;
    @Column
    private String forename;

    @Column
    private String surname;

    @Id
    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private String passwordResetToken;

    @Column
    private LocalDateTime passwordResetTokenExpiration;

    @Column
    private String registrationToken;

    // Credentials that a user can use
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Credential> permittedCredentials;

    // Credentials that an issuer can issue to other users
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Credential> issuableCredentials;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    @Column
    private Boolean activated;

    @Column Boolean registered;

    public SesamUser() {
    }

    public SesamUser(
            final String forename,
            final String surname,
            final String email,
            final String password
    ) {
        this.forename = forename;
        this.surname = surname;
        this.email = email;
        this.username = email;
        this.password = password;
        this.permittedCredentials = new ArrayList<>();
        this.issuableCredentials = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.activated = false;
        this.registered = false;
    }

    @JsonIgnore
    public Collection<Credential> getIssuableCredentials() {
        if (roles == null) return new ArrayList<>();
        if (roles.contains(ROLE_ISSUER)) return issuableCredentials;
        return new ArrayList<>();
    }

    public void setIssuableCredentials(Collection<Credential> issuableCredentials) {
        this.issuableCredentials = issuableCredentials;
    }


    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setForename(final String forename) {
        this.forename = forename;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public String getEmail() {
        return email;
    }


    public void setUsername(final String username) {
        this.username = username;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String generatePasswordResetToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        passwordResetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        passwordResetTokenExpiration = LocalDateTime.now().plusHours(24);
        return passwordResetToken;
    }

    public boolean isResetTokenValid(String token) {
        if (passwordResetToken == null || !passwordResetToken.equals(token)) {
            return false;
        }
        if (!passwordResetTokenExpiration.isAfter(LocalDateTime.now())) {
            return false;
        }
        passwordResetToken = null;
        return true;
    }

    public String generateRegistrationToken() {
        if (registrationToken != null) return registrationToken;
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        registrationToken = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        return registrationToken;
    }

    public boolean isRegistrationTokenValid(String token) {
        if (registrationToken == null || !registrationToken.equals(token)) {
            return false;
        }
        registrationToken = null;
        return true;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(roles.toArray(new String[0]));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated != null && activated;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered != null && registered;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    public void addRole(final String role) {
        if (role == null) return;
        if (roles == null)
            this.roles = new ArrayList<>();
        this.roles.add(role);
    }

    public void removeRole(final String role) {
        if (role == null) return;
        if (roles == null)
            this.roles = new ArrayList<>();
        this.roles.remove(role);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SesamUser sesamUser = (SesamUser) o;
        return Objects.equals(forename, sesamUser.forename) &&
                Objects.equals(surname, sesamUser.surname) &&
                Objects.equals(email, sesamUser.email) &&
                Objects.equals(password, sesamUser.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(forename, surname, email, password);
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @JsonIgnore
    public boolean isAuthenticated() {
        return (isActivated() && isRegistered());
    }
}
