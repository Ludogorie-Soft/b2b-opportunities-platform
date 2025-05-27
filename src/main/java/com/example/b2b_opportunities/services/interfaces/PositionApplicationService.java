package com.example.b2b_opportunities.services.interfaces;

import com.example.b2b_opportunities.dto.requestDtos.PositionApplicationRequestDto;
import com.example.b2b_opportunities.dto.responseDtos.CompanyApplicationResponseDto;
import com.example.b2b_opportunities.dto.responseDtos.PositionApplicationResponseDto;
import com.example.b2b_opportunities.entity.PositionApplication;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PositionApplicationService {
    PositionApplicationResponseDto applyForPosition(Authentication authentication, PositionApplicationRequestDto requestDto);

    PositionApplicationResponseDto uploadCV(MultipartFile file, Long applicationId);

    boolean doesCVExist(Long positionApplicationId);

    String returnUrlIfCVExists(Long positionApplicationId);

    List<PositionApplicationResponseDto> getApplicationsForMyPositions(Authentication authentication);

    List<PositionApplicationResponseDto> getMyApplications(Authentication authentication);

    PositionApplicationResponseDto acceptApplication(Authentication authentication, Long applicationId);

    PositionApplicationResponseDto rejectApplication(Authentication authentication, Long applicationId);

    PositionApplicationResponseDto getApplicationById(Authentication authentication, Long applicationId);

    PositionApplicationResponseDto updateApplication(Authentication authentication, MultipartFile file, Long applicationId, Long talentId);

    List<PositionApplication> getApplicationsSinceLastWorkday();

    List<CompanyApplicationResponseDto> getMyApplicationsOverall(Authentication authentication);

    void cancelApplication(Authentication authentication, Long applicationId);
}
