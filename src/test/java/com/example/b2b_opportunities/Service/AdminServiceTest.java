package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.BaseTest;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AdminServiceTest extends BaseTest {
    private final UserRepository userRepository;
    private final AdminService adminService;
    private User user;

    @Autowired
    public AdminServiceTest(UserRepository userRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    @BeforeEach
    void setup() {
        user = User.builder()
                .firstName("test")
                .lastName("test")
                .email("test@abv.bg")
                .username("test")
                .build();
    }

    @Test
    void shouldApproveUnapprovedUser() {
        user.setApproved(false);
        user = userRepository.save(user);

        UserResponseDto approvedUser = adminService.approve(user.getId());

        assertNotNull(approvedUser);
        assertTrue(approvedUser.isApproved());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(updatedUser.isApproved());
    }

    @Test
    void shouldNotChangeAlreadyApprovedUser() {
        user.setApproved(true);
        user = userRepository.save(user);

        UserResponseDto approvedUser = adminService.approve(user.getId());

        assertNotNull(approvedUser);
        assertTrue(approvedUser.isApproved());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(updatedUser.isApproved());
    }

    @Test
    void shouldGetAllNonApprovedUsers() {
        User user1 = User.builder()
                .firstName("User1")
                .lastName("Test1")
                .email("user1@abv.bg")
                .username("user1")
                .isApproved(false)
                .build();

        User user2 = User.builder()
                .firstName("User2")
                .lastName("Test2")
                .email("user2@abv.bg")
                .username("user2")
                .isApproved(true)  // Approved - to make sure it's not being count
                .build();

        User user3 = User.builder()
                .firstName("User3")
                .lastName("Test3")
                .email("user3@abv.bg")
                .username("user3")
                .isApproved(false)
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));

        List<UserResponseDto> nonApprovedUsers = adminService.getAllNonApprovedUsers();

        assertNotNull(nonApprovedUsers);
        assertEquals(2, nonApprovedUsers.size()); // user 1 and 3
        assertTrue(nonApprovedUsers.stream().noneMatch(user -> user.isApproved()));
    }
}