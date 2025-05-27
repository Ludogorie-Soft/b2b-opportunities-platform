package com.example.b2b_opportunities.dto.requestDtos;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRequestDto {
    @Column(unique = true)
    @NotEmpty(message = "username cannot be empty")
    @Size(min = 3, max = 40, message = "username must be between 3 and 40 characters long")
    private String username;

    @NotEmpty
    @Size(min = 3, max = 40, message = "firstName must be between 3 and 40 characters long")
    private String firstName;

    @NotEmpty
    @Size(min = 3, max = 40, message = "lastName must be between 3 and 40 characters long")
    private String lastName;
    @Column(unique = true)
    @NotEmpty(message = "email cannot be empty")
    @Email(message = "Invalid email format.")
    private String email;

    @NotEmpty(message = "password cannot be empty")
    @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters long")
    private String password;

    private String repeatedPassword;
}