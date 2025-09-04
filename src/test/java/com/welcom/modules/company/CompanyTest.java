package com.welcom.modules.company;

import com.welcom.modules.account.Account;
import com.welcom.modules.account.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CompanyTest {

    Company company;
    Account account;
    UserAccount userAccount;

    @BeforeEach
    void beforeEach() {
        company = new Company();
        account = new Account();
        account.setNickname("nick");
        account.setPassword("123");
        userAccount = new UserAccount(account);
    }

    @DisplayName("회사를 공개했고 모집중이고, 이미 멤버나 관리자가 아니라면 회사 지원 가능")
    @Test
    void isJoinable() {
        company.setPublished(true);
        company.setRecruiting(true);
        assertTrue(company.isJoinable(userAccount));
    }

    @DisplayName("회사를 공개했고 지원모집 중이더라도, 회사 관리자는 회사 지원이 불필요하다.")
    @Test
    void isJoinable_false_for_manager() {
        company.setPublished(true);
        company.setRecruiting(true);
        company.addManager(account);
        assertFalse(company.isJoinable(userAccount));
    }

    @DisplayName("회사를 공개했고 지원모집 중이더라도, 회사 구성원은 회사 재지원이 불필요하다. ")
    @Test
    void isJoinable_false_for_member() {
        company.setPublished(true);
        company.setRecruiting(true);
        company.addMember(account);
        assertFalse(company.isJoinable(userAccount));
    }

    @DisplayName("회사가 비공개이거나 인원 모집 중이 아니면 회사 가입이 불가능하다.")
    @Test
    void isJoinable_false_for_non_recruiting_company() {
        company.setPublished(true);
        company.setRecruiting(false);
        assertFalse(company.isJoinable(userAccount));

        company.setPublished(false);
        company.setRecruiting(true);
        assertFalse(company.isJoinable(userAccount));
    }

    @DisplayName("회사 관리자인지 확인")
    @Test
    void isManager() {
        company.addManager(account);
        assertTrue(company.isManager(userAccount));
    }

    @DisplayName("회사 멤버인지 확인")
    @Test
    void isMember() {
        company.addMember(account);
        assertTrue(company.isMember(userAccount));
    }
}
