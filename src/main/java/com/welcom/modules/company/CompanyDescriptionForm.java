package com.welcom.modules.company;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class CompanyDescriptionForm {

    @NotBlank
    @Length(max = 100)
    private String shortDescription;
    @NotBlank
    private String fullDescription;
}
