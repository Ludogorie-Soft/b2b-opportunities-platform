package com.example.b2b_opportunities.Config;

import com.example.b2b_opportunities.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsServiceImpl);
        authProvider.setPasswordEncoder(SecurityConfig.passwordEncoder());

        return authProvider;
    }
}