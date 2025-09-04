package com.welcom.modules.company.validator;

import com.welcom.modules.company.CompanyForm;
import com.welcom.modules.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CompanyFormValidator implements Validator {

    private final CompanyRepository companyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CompanyForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CompanyForm companyForm = (CompanyForm) target;
        if (companyRepository.existsByPath(companyForm.getPath())) {
            errors.rejectValue("path","wrong.path","해당 회사 경로값을 사용할 수 없습니다.");
        }
    }
}
