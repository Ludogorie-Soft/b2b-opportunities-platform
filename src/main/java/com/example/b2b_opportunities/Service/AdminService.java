package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.common.NotFoundException;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    public UserResponseDto approve(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        if (user.isApproved()) {
            return UserMapper.toResponseDto(user);
        }
        user.setApproved(true);
        return UserMapper.toResponseDto(userRepository.save(user));
    }

    public List<UserResponseDto> getAllNonApprovedUsers() {
        List<User> users = userRepository.findByIsApprovedFalse();
        return UserMapper.toResponseDtoList(users);
    }
}
