package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Entity.Employer;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.EmployerRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws BadCredentialsException {
        try {
            return loadUserByEmailOrUsername(usernameOrEmail);
        } catch (BadCredentialsException e) {
            return loadEmployerByEmailOrUsername(usernameOrEmail);
        }
    }

    private UserDetails loadUserByEmailOrUsername(String emailOrUsername) {
        Optional<User> optionalUser = userRepository.findByEmail(emailOrUsername);
        if (optionalUser.isPresent()) {
            return new MyUserDetails(optionalUser.get());
        }
        optionalUser = userRepository.findByUsername(emailOrUsername);
        if (optionalUser.isPresent()) {
            return new MyUserDetails(optionalUser.get());
        }
        throw new BadCredentialsException("Invalid credentials");
    }

    private UserDetails loadEmployerByEmailOrUsername(String emailOrUsername) {
        Optional<Employer> optionalEmployer = employerRepository.findByEmail(emailOrUsername);
        if (optionalEmployer.isPresent()) {
            if (!optionalEmployer.get().isEnabled()) {
                throw new DisabledException("User is not enabled");
            }
            return new MyUserDetails(optionalEmployer.get());
        }
        optionalEmployer = employerRepository.findByUsername(emailOrUsername);
        if (optionalEmployer.isPresent()) {
            return new MyUserDetails(optionalEmployer.get());
        }
        // Throw a generic exception so that hackers won't be able to brute-force attack
        throw new BadCredentialsException("Invalid credentials");
    }
}
