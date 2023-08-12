package com.gpse.basis.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration //<1>
@EnableMethodSecurity(securedEnabled = true) //<2>
public class MethodSecurityConfig {
    // ... //<3>
}
