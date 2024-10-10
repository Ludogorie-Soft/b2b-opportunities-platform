package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Repository.PositionRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PositionRoleService {
    private final PositionRoleRepository positionRoleRepository;
}