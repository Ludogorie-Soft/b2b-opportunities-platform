package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;

    public String createCompany(Authentication authentication,
                                CompanyRequestDto companyRequestDto,
                                MultipartFile image,
                                MultipartFile banner) {
        if (authentication == null) throw new AuthenticationFailedException("User not authenticated");
        if (image.isEmpty()) throw new NotFoundException("Image not uploaded"); //this will be improved
        Company company = companyMapper.toCompany(companyRequestDto);
        company = companyRepository.save(company);
        company.setImage("testImage");
        company.setBanner("testBanner");
        if (!areEmailsAreTheSame(authentication, company.getEmail())) {
            company.setEmailVerification(EmailVerification.PENDING);
        } else {
            company.setEmailVerification(EmailVerification.ACCEPTED);
        }
        company.setUsers(List.of(getCurrentUser(authentication)));
        companyRepository.save(company);

        return "Company created successfully";
    }

    private boolean areEmailsAreTheSame(Authentication authentication, String companyEmail) {
        User currentUser = getCurrentUser(authentication);
        return currentUser.getEmail().equals(companyEmail);
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
