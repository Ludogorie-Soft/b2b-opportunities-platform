package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
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

import java.util.ArrayList;
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
        validateCompanyRequest(companyRequestDto);
        Company company = companyMapper.toCompany(companyRequestDto);
        company = companyRepository.save(company);

        company.setImage("testImage");
        company.setBanner("testBanner");

        if (!areEmailsAreTheSame(authentication, company.getEmail())) {
            company.setEmailVerification(EmailVerification.PENDING); //This should send a confirmation mail
        } else {
            company.setEmailVerification(EmailVerification.ACCEPTED);
        }

        User user = getCurrentUser(authentication);
        user.setCompany(company);
        company.getUsers().add(user);

        userRepository.save(user);
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

    private void validateCompanyRequest(CompanyRequestDto companyRequestDto) {
        if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email already registered");
        if (companyRepository.findByWebsite(companyRequestDto.getWebsite()).isPresent())
            throw new AlreadyExistsException("Website already registered");
        if (companyRepository.findByLinkedIn(companyRequestDto.getLinkedIn()).isPresent())
            throw new AlreadyExistsException("LinkedIn already registered");
    }
}
