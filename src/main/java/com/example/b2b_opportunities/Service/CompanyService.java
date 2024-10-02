package com.example.b2b_opportunities.Service;

import com.example.b2b_opportunities.Dto.Request.CompanyRequestDto;
import com.example.b2b_opportunities.Dto.Response.CompaniesAndUsersResponseDto;
import com.example.b2b_opportunities.Dto.Response.UserResponseDto;
import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.User;
import com.example.b2b_opportunities.Exception.AlreadyExistsException;
import com.example.b2b_opportunities.Exception.AuthenticationFailedException;
import com.example.b2b_opportunities.Exception.NotFoundException;
import com.example.b2b_opportunities.Mapper.CompanyMapper;
import com.example.b2b_opportunities.Mapper.UserMapper;
import com.example.b2b_opportunities.Repository.CompanyRepository;
import com.example.b2b_opportunities.Repository.UserRepository;
import com.example.b2b_opportunities.Static.EmailVerification;
import com.example.b2b_opportunities.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
        if (image == null || image.isEmpty()) throw new NotFoundException("Image is missing or empty");
        companyRequestDto.setName(companyRequestDto.getName().trim().replaceAll("\\s+", " "));
        validateCompanyRequest(companyRequestDto);
        Company company = companyMapper.toCompany(companyRequestDto);

        // iD -> img / banner

        if (getCurrentUser(authentication).getEmail().equals(company.getEmail())) {
            company.setEmailVerification(EmailVerification.ACCEPTED);
        } else {
            company.setEmailVerification(EmailVerification.PENDING); //This should send a confirmation mail

        }
        company = companyRepository.save(company);

        User user = getCurrentUser(authentication);
        user.setCompany(company);
        userRepository.save(user);

        //save images

        return "Company created successfully";
    }

    public List<CompaniesAndUsersResponseDto> getCompaniesAndUsers() {
        List<CompaniesAndUsersResponseDto> responseList = new ArrayList<>();

        List<Company> companies = companyRepository.findAll();
        List<User> users = userRepository.findAll();

        for (Company company : companies) {
            List<UserResponseDto> userList = new ArrayList<>();
            CompaniesAndUsersResponseDto response = new CompaniesAndUsersResponseDto();
            for (User user : users) {
                if (user.getCompany().getId().equals(company.getId())) {
                    response.setCompany(CompanyMapper.toCompanyResponseDto(company));
                    userList.add(UserMapper.toResponseDto(user));
                }
            }
            response.setUsers(userList);
            responseList.add(response);
        }
        return responseList;
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private void validateCompanyRequest(CompanyRequestDto companyRequestDto) {
        if (companyRepository.findByNameIgnoreCase(companyRequestDto.getName()).isPresent())
            throw new AlreadyExistsException("Name already registered");
        if (companyRepository.findByEmail(companyRequestDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email already registered");
        if (companyRepository.findByWebsite(companyRequestDto.getWebsite()).isPresent())
            throw new AlreadyExistsException("Website already registered");
        if (companyRepository.findByLinkedIn(companyRequestDto.getLinkedIn()).isPresent())
            throw new AlreadyExistsException("LinkedIn already registered");
    }
}
