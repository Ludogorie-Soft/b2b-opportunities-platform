package com.example.b2b_opportunities;

import com.example.b2b_opportunities.Entity.Employer;
import com.example.b2b_opportunities.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MyUserDetails implements UserDetails {

    private final Object principal;
    private final boolean isEmployer;

    public MyUserDetails(User user) {
        this.principal = user;
        this.isEmployer = false;
    }

    public MyUserDetails(Employer employer) {
        this.principal = employer;
        this.isEmployer = true;
    }

    public User getUser() {
        if (isEmployer) {
            return null;
        }
        return (User) principal;
    }

    public Employer getEmployer() {
        if (isEmployer) {
            return (Employer) principal;
        }
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (isEmployer) {
            return List.of(new SimpleGrantedAuthority(getEmployer().getRole().getName()));
        }
        return List.of(new SimpleGrantedAuthority(getUser().getRole().getName()));
    }

    @Override
    public String getPassword() {
        if (isEmployer) {
            return getEmployer().getPassword();
        }
        return getUser().getPassword();
    }

    @Override
    public String getUsername() {
        if (isEmployer) {
            return getEmployer().getUsername();
        }
        return getUser().getUsername();
    }


    public String getEmail() {
        if (isEmployer) {
            return getEmployer().getEmail();
        }
        return getUser().getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (isEmployer) {
            return getEmployer().isEnabled();
        }
        return getUser().isEnabled();
    }
}