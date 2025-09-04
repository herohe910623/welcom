package com.welcom.modules.company;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CompanyForm {

    public static final String VALID_PATH_PATTERN = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$"; // _ 와 - 도 가능

    @NotBlank
    @Length(min = 2, max = 20)
    @Pattern(regexp = VALID_PATH_PATTERN)
    private String path;

    @NotBlank
    @Length(max = 50)
    private String name;

    private String companyLink;
    @NotBlank
    @Length(max = 100)
    private String shortDescription;
    @NotBlank
    private String fullDescription;
}
