package com.example.b2b_opportunities;

import com.example.b2b_opportunities.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws BadCredentialsException {
        return userRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }
}
