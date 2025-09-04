package com.welcom.modules.company;

import com.welcom.modules.account.Account;
import com.welcom.modules.account.CurrentAccount;
import com.welcom.modules.company.validator.CompanyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyFormValidator companyFormValidator;
    private final ModelMapper modelMapper;
    private final CompanyRepository companyRepository;

    @InitBinder("companyForm")
    public void companyFormBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(companyFormValidator);
    }

    @GetMapping("/new-company")
    public String newCompanyForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new CompanyForm());
        return "company/form";
    }

    @PostMapping("/new-company")
    public String newCompanySubmit(@CurrentAccount Account account, @Valid CompanyForm companyForm, Errors errors) {
        if (errors.hasErrors()) {
            return "company/form";
        }
        Company newCompany = companyService.createNewCompany(modelMapper.map(companyForm,Company.class),account);
        return "redirect:/company/" + URLEncoder.encode(newCompany.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/company/{path}")
    public String viewCompany(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Company company = companyService.getCompany(path);
        model.addAttribute(account);
        model.addAttribute(company);
        return "company/view";
    }

    @GetMapping("/company/{path}/members")
    public String viewCompanyMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Company company = companyService.getCompany(path);
        model.addAttribute(account);
        model.addAttribute(company);
        return "company/members";
    }

    @GetMapping("/company/{path}/join") //TODO Should to Changing PostMapping
    public String joinCompany(@CurrentAccount Account account, @PathVariable String path) {
        Company company = companyRepository.findCompanyWithMembersByPath(path);
        companyService.addMember(company, account);
        return "redirect:/company/" + company.getEncodedPath() + "/members";
    }
    @GetMapping("/company/{path}/remove") //TODO Should to Changing PostMapping
    public String removeCompany(@CurrentAccount Account account, @PathVariable String path) {
        Company company = companyRepository.findCompanyWithMembersByPath(path);
        companyService.removeMember(company, account);
        return "redirect:/company/" + company.getEncodedPath() + "/members";
    }

}
