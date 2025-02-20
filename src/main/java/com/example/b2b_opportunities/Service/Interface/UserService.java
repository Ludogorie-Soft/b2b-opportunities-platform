package com.example.b2b_opportunities.Service.Interface;

import com.example.b2b_opportunities.Entity.User;
import org.springframework.security.core.Authentication;

public interface UserService {
    User getCurrentUserOrThrow(Authentication authentication);

    User getUserByEmailOrThrow(String email);
}
