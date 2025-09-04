package com.welcom.modules.account;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account herohe = new Account();
        herohe.setNickname(nickname);
        herohe.setEmail(nickname + "@email.com");
        accountRepository.save(herohe);
        return herohe;
    }
}
