package com.example.accounts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {
    @Bean UserDetailsService users(@Value("${demo.password}") String password) {
        var hash = new BCryptPasswordEncoder().encode(password);
        return new InMemoryUserDetailsManager(
            User.withUsername("operator").password(hash).roles("OPERATOR").build(),
            User.withUsername("auditor").password(hash).roles("AUDITOR").build());
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        // Local machine-to-machine demo. No browser session or cookie authentication.
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/accounts/**").hasAnyRole("OPERATOR", "AUDITOR")
                .requestMatchers("/api/accounts/**").permitAll()
                .anyRequest().denyAll())
            .httpBasic(Customizer.withDefaults()).build();
    }
}
