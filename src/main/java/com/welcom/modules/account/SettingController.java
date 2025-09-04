package com.welcom.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.welcom.modules.account.form.NicknameForm;
import com.welcom.modules.account.form.NotificationsForm;
import com.welcom.modules.account.form.PasswordForm;
import com.welcom.modules.account.form.ProfileForm;
import com.welcom.modules.tag.Tag;
import com.welcom.modules.tag.TagForm;
import com.welcom.modules.zone.Zone;
import com.welcom.modules.tag.TagRepository;
import com.welcom.modules.zone.ZoneRepository;
import com.welcom.modules.tag.TagService;
import com.welcom.modules.zone.ZoneService;
import com.welcom.modules.account.validator.PasswordFormValidator;
import com.welcom.modules.zone.ZoneForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingController {

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    private final AccountService accountService;
    private final TagService tagService;
    private final ZoneService zoneService;
    private final ZoneRepository zoneRepository;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/settings/profile")
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentAccount Account account, @Valid @ModelAttribute("profileForm") ProfileForm profile, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }
        accountService.updateProfile(account, profile);
        redirectAttributes.addFlashAttribute("message", "프로필 수정을 완료했습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/password")
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String updatePasswordForm(@CurrentAccount Account account, @Valid @ModelAttribute PasswordForm passwordForm, Errors errors,
                                     Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }
        accountService.updatePassword(account, passwordForm);
        redirectAttributes.addFlashAttribute("message", "패스워드 수정을 완료했습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/settings/notifications")
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationsForm.class));
        return "settings/notifications";
    }

    @PostMapping("/settings/notifications")
    public String updateNotifications(@CurrentAccount Account account, @Valid @ModelAttribute NotificationsForm notificationsForm, Errors errors,
                                      Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/notifications";
        }
        accountService.updateNotifications(account, notificationsForm);
        redirectAttributes.addFlashAttribute("message", "알림 수정을 완료했습니다.");
        return "redirect:/settings/notifications";
    }

    @GetMapping("/settings/account")
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String updateAccount(@CurrentAccount Account account, @Valid @ModelAttribute NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/account";
        }
        accountService.updateNickname(account, nicknameForm);
        redirectAttributes.addFlashAttribute("message", "닉네임 변경을 완료했습니다.");
        return "redirect:/settings/account";
    }

    @GetMapping("/settings/tags")
    public String addTagsForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
        return "settings/tags";
    }

    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTags(@CurrentAccount Account account, @RequestBody TagForm tagForm) {

        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTags(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String tagTitle = tagForm.getTagTitle();
        Tag tag = tagService.findOrCreateNew(tagTitle);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/zones")
    public String updateZoneForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));
//        List<String> whitelist = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        List<String> whitelist = zoneService.findWhitelist();
        model.addAttribute("whitelist", objectMapper.writeValueAsString(whitelist));
        return "settings/zones";
    }

    @PostMapping("/settings/zones/add")
    @ResponseBody
    public ResponseEntity addZones(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
         Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
         if (zone == null) {
             ResponseEntity.badRequest().build();
         }
         accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/settings/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            ResponseEntity.badRequest().build();
        }
        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }

}
