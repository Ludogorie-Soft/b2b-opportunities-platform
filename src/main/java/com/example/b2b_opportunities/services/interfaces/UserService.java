package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.entity.User;
import org.springframework.security.core.Authentication;

public interface UserService {
    User getCurrentUserOrThrow(Authentication authentication);

    User getUserByEmailOrThrow(String email);
}
