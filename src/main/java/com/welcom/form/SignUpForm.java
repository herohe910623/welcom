package com.welcom.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {
    @NotBlank
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{3,20}$")    // _ 와 - 도 가능
    private String nickname;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Length(min = 8, max = 50)
    private String password;
}
