package com.example.b2b_opportunities.Controller;

import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/approve/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto approveUser(@PathVariable("id") Long id) {
        return adminService.approve(id);
    }

    @GetMapping("/get-non-approved")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDto> getAllNonApprovedUsers() {
        return adminService.getAllNonApprovedUsers();
    }
}