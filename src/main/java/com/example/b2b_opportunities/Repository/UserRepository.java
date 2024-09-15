package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isApproved = false AND size(u.posts) > 0")
    List<User> findUsersWithUnapprovedStatusAndPosts();

}
