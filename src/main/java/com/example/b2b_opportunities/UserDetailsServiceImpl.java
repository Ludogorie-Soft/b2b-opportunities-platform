package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws BadCredentialsException {
        Optional<User> optionalUser = userRepository.findByEmail(usernameOrEmail);
        if (optionalUser.isPresent()) {
            return new UserDetailsImpl(optionalUser.get());
        }
        optionalUser = userRepository.findByUsername(usernameOrEmail);
        if (optionalUser.isPresent()) {
            return new UserDetailsImpl(optionalUser.get());
        }
        throw new BadCredentialsException("Invalid credentials");
    }
}
