package com.welcom.modules.company;

import com.welcom.infra.MockMvcTest;
import com.welcom.modules.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@MockMvcTest
public class CompanyControllerTest{

    @Autowired MockMvc mockMvc;
    @Autowired CompanyService companyService;
    @Autowired CompanyRepository companyRepository;
    @Autowired AccountRepository accountRepository;

}
