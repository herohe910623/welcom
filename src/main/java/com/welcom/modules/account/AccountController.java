package com.welcom.modules.account;

import com.welcom.modules.account.form.SignUpForm;
import com.welcom.modules.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkedEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if (account == null) {
            model.addAttribute("error", "wrong.error");
            System.out.println("account is null");
            return view;
        }
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            System.out.println("token not equals");
            return view;
        }
        accountService.completeSignUp(account);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }
    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentAccount Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "메일 전송 버튼은 1 시간에 한번만 할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        accountService.sendConfirmCheckEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, @CurrentAccount Account account, Model model) {
        /**
         * 프로필 수정에 들어가야 할 값
         * 프로필 이미지 profileImage
         * 한줄소개 bio
         * 링크 link
         * 회사 company
         * 직군 occupation
         * 위치 location
         * 이메일 email
         * 가입한 시간 joinedAt
         */
        Account accountToView = accountService.getAccount(nickname);
        model.addAttribute(accountToView);
        model.addAttribute("isOwner",accountToView.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm() {
       return "/account/email-login";
    }

    @PostMapping("/email-login")
    public String emailLoginSubmit(String email, Model model, RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
           model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
           return "account/email-login";
        }
        accountService.sendLoginLink(account);
        redirectAttributes.addFlashAttribute("message","이메일 전송을 완료했습니다.");

        return "/account/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(Model model,String token, String email) {
        Account account = accountRepository.findByEmail(email);
        if (account == null || !account.isValidToken(token)){
            model.addAttribute("error","유효한 계정이 아닙니다.");
            return "account/logged-by-email";
        }
        accountService.login(account);
        return "account/logged-by-email";
    }
}