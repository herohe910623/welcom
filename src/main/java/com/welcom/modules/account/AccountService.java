package com.welcom.modules.account;

import com.welcom.infra.config.AppProperties;
import com.welcom.infra.mail.EmailMessage;
import com.welcom.infra.mail.EmailService;
import com.welcom.modules.account.form.*;
import com.welcom.modules.zone.Zone;
import com.welcom.modules.tag.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    //    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);

        //        Account account = Account.builder()
        //                .nickname(signUpForm.getNickname())
        //                .email(signUpForm.getEmail())
        //                .password(passwordEncoder.encode(signUpForm.getPassword()))
        //                .companyCreatedByWeb(true)
        //                .companyUpdatedByWeb(true)
        //                .companyUpdatedByWeb(true)
        //                .emailTokenGeneratedAt(LocalDateTime.now())
        //                .build();
        //        return accountRepository.save(account);
    }

    public void sendConfirmCheckEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken()
                + "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "welcom. 서비스를 이용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link.html", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("회원가입 인증")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendConfirmCheckEmail(newAccount);
        return newAccount;
    }


    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        // 확인해주어야 할것 이메일이 아닌경우 닉네임으로도 확인 하고 그래도 에러이면 exception 던져주기
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }
        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if (account == null) {
            throw new IllegalArgumentException(nickname + " 사용자가 존재하지 않습니다.");
        }
        return account;
    }

    public void updateProfile(Account account, ProfileForm profile) {
        //        account.setBio(profile.getBio());
        //        account.setUrl(profile.getUrl());
        //        account.setOccupation(profile.getOccupation());
        //        account.setLocation(profile.getLocation());
        //        account.setProfileImage(profile.getProfileImage());
        modelMapper.map(profile, account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, PasswordForm passwordForm) {
        account.setPassword(passwordEncoder.encode(passwordForm.getNewPassword()));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, NotificationsForm notificationsForm) {
        //        account.setCompanyCreatedByEmail(notificationsForm.isCompanyCreatedByEmail());
        //        account.setCompanyCreatedByWeb(notificationsForm.isCompanyCreatedByWeb());
        //        account.setCompanyRecruitResultByEmail(notificationsForm.isCompanyRecruitResultByEmail());
        //        account.setCompanyRecruitResultByWeb(notificationsForm.isCompanyRecruitResultByWeb());
        //        account.setCompanyUpdatedByEmail(notificationsForm.isCompanyUpdatedByEmail());
        //        account.setCompanyUpdatedByWeb(notificationsForm.isCompanyUpdatedByWeb());
        modelMapper.map(notificationsForm, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, NicknameForm nicknameForm) {
        account.setNickname(nicknameForm.getNickname());
        accountRepository.save(account);
    }

    public void sendLoginLink(Account account) {
        //        account.generateEmailCheckToken();
        //        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        //        simpleMailMessage.setTo(account.getEmail());
        //        simpleMailMessage.setSubject("welcom , login link");
        //        simpleMailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        //        javaMailSender.send(simpleMailMessage);
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일로 로그인하기");
        context.setVariable("message", "welcom. 서비스를 이용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link.html", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("welcom. , login link")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);

    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }
}

