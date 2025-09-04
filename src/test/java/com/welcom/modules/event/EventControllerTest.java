package com.welcom.modules.event;

import com.welcom.modules.account.WithAccount;
import com.welcom.modules.account.Account;
import com.welcom.modules.company.Company;
import com.welcom.modules.account.AccountRepository;
import com.welcom.modules.company.CompanyRepository;
import com.welcom.modules.company.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private EventService eventService;

    private Account createAccount(String nickname) {
        Account herohe = new Account();
        herohe.setNickname(nickname);
        herohe.setEmail(nickname + "@email.com");
        accountRepository.save(herohe);
        return herohe;
    }
    private Company createCompany(String path, Account account) {
        Company company = new Company();
        company.setPath(path);
        companyService.createNewCompany(company, account);
        return company;
    }
    private Event createEvent(String eventTitle, EventType eventType, int limit, Company company, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event,company,account);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("herohe")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account herohe2 = createAccount("herohe2");
        Company company = createCompany("test-company", herohe2);
        Event event = createEvent("test-event", EventType.FCFS, 2, company, herohe2);

        mockMvc.perform(post("/company/" + company.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/company/" + company.getPath() + "/events/" + event.getId()));

        Account account = accountRepository.findByNickname("herohe");
        assertTrue(enrollmentRepository.findByEventAndAccount(event,account).isAccepted());
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중(이미 인원이 꽉차서)")
    @WithAccount("herohe")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account herohe2 = createAccount("herohe2");
        Company company = createCompany("test-company", herohe2);
        Event event = createEvent("test-event", EventType.FCFS, 2, company, herohe2);

        Account one = createAccount("one");
        Account two = createAccount("two");
        eventService.newEnrollment(event,one);
        eventService.newEnrollment(event,two);

        mockMvc.perform(post("/company/" + company.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/company/" + company.getPath() + "/events/" + event.getId()));

        Account account = accountRepository.findByNickname("herohe");
        assertFalse(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    @Test
    @DisplayName("참가신청 확장자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("herohe")
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Account one = createAccount("one");
        Account two = createAccount("two");
        Company company = createCompany("test-company", one);
        Event event = createEvent("test-event", EventType.FCFS, 2, company, one);

        eventService.newEnrollment(event, one);
        eventService.newEnrollment(event, herohe);
        eventService.newEnrollment(event, two);

        assertTrue(enrollmentRepository.findByEventAndAccount(event, one).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, herohe).isAccepted());
        assertFalse(enrollmentRepository.findByEventAndAccount(event, two).isAccepted());

        mockMvc.perform(post("/company/" + company.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/company/" + company.getPath() + "/events/" + event.getId()));

        assertTrue(enrollmentRepository.findByEventAndAccount(event,one).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event,two).isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event,herohe));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확장자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("herohe")
    void not_accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Account one = createAccount("one");
        Account two = createAccount("two");
        Company company = createCompany("test-company", one);
        Event event = createEvent("test-event", EventType.FCFS, 2, company, one);

        eventService.newEnrollment(event, one);
        eventService.newEnrollment(event, two);
        eventService.newEnrollment(event, herohe);

        assertTrue(enrollmentRepository.findByEventAndAccount(event,one).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, two).isAccepted());
        assertFalse(enrollmentRepository.findByEventAndAccount(event, herohe).isAccepted());

        mockMvc.perform(post("/company/" + company.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/company/" + company.getPath() + "/events/" + event.getId()));

        assertTrue(enrollmentRepository.findByEventAndAccount(event,one).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event,two).isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event, herohe));

    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("herohe")
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account one = createAccount("one");
        Company company = createCompany("test-company", one);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, company, one);

        mockMvc.perform(post("/company/" + company.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/company/" + company.getPath() + "/events/" + event.getId()));

        Account herohe = accountRepository.findByNickname("herohe");
        assertFalse(enrollmentRepository.findByEventAndAccount(event,herohe).isAccepted());
    }



}