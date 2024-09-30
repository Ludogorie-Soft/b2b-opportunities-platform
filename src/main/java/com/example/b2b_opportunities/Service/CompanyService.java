package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompanyResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyResponseDto createCompany(Authentication authentication,
                                            CompanyRequestDto companyRequestDto,
                                            MultipartFile image,
                                            MultipartFile banner) {
        if (authentication == null) throw new AuthenticationFailedException("User not authenticated");
        if (image.isEmpty()) throw new NotFoundException("Image not uploaded"); //this will be improved
        Company company = companyRepository.save(CompanyMapper.toCompany(companyRequestDto));
        company.setImage("testImage");
        company.setBanner("testBanner");
        if (!areEmailsAreTheSame(authentication, company.getEmail())) {
            company.setEmailVerification(EmailVerification.PENDING);
        } else {
            company.setEmailVerification(EmailVerification.ACCEPTED);
        }
        company.setUsers(List.of(getCurrentUser(authentication)));
        companyRepository.save(company);
        return CompanyMapper.toCompanyResponseDto(company);
    }

    private boolean areEmailsAreTheSame(Authentication authentication, String companyEmail) {
        User currentUser = getCurrentUser(authentication);
        return currentUser.getEmail().equals(companyEmail);
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getAuthorities();
        return userDetails.getUser();
    }
}
