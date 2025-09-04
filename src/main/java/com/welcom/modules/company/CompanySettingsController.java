package com.welcom.modules.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.welcom.modules.account.Account;
import com.welcom.modules.account.CurrentAccount;
import com.welcom.modules.tag.TagForm;
import com.welcom.modules.zone.ZoneForm;
import com.welcom.modules.tag.TagRepository;
import com.welcom.modules.zone.ZoneRepository;
import com.welcom.modules.tag.TagService;
import com.welcom.modules.zone.ZoneService;
import com.welcom.modules.tag.Tag;
import com.welcom.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/company/{path}/settings")
@RequiredArgsConstructor
public class CompanySettingsController {

    private final CompanyService companyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final ZoneService zoneService;

    @GetMapping("/description")
    public String viewCompanySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Company company = companyService.getCompanyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(company);
        model.addAttribute(modelMapper.map(company, CompanyDescriptionForm.class));
        return "company/settings/description";
    }

    @PostMapping("/description")
    public String updateCompanyInfo(@CurrentAccount Account account, @PathVariable String path,
                                    @Valid CompanyDescriptionForm companyDescriptionForm, Errors errors,
                                    Model model, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdate(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(company);
            return "company/settings/description";
        }
        companyService.updateCompanyDescription(company, companyDescriptionForm);
        redirectAttributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/company/" + getPath(path) + "/settings/description";
    }

    @GetMapping("/banner")
    public String companyImageForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Company company = companyService.getCompanyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(company);
        return "company/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String companyImageEnable(@CurrentAccount Account account, @PathVariable String path) {
        Company company = companyService.getCompanyToUpdate(account, path);
        companyService.enableCompanyBanner(company);
        return "redirect:/company/" + company.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String companyImageDisable(@CurrentAccount Account account, @PathVariable String path) {
        Company company = companyService.getCompanyToUpdate(account, path);
        companyService.disableCompanyBanner(company);
        return "redirect:/company/" + company.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner")
    public String companyImageSubmit(@CurrentAccount Account account, @PathVariable String path, Model model,
                                     String image, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdate(account, path);
        companyService.updateCompanyImage(company, image);
        redirectAttributes.addFlashAttribute("message", "배너 이미지를 변경하였습니다.");
        return "redirect:/company/" + company.getEncodedPath() + "/settings/banner";
    }

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/tags")
    public String companyTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Company company = companyService.getCompanyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(company);

        model.addAttribute("tags", company.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
        return "company/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity companyAddTags(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Company company = companyService.getCompanyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        companyService.addTags(company, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity companyRemoveTags(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Company company = companyService.getCompanyToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        companyService.removeTags(company, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String companyZonesForm(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Company company = companyService.getCompanyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(company);
        model.addAttribute("zones", company.getZones().stream().map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return "company/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity companyAddZones(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Company company = companyService.getCompanyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            ResponseEntity.badRequest().build();
        }
        companyService.addZones(company, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity companyRemoveZones(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Company company = companyService.getCompanyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            ResponseEntity.badRequest().build();
        }
        companyService.removeZones(company, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/company")
    public String companySettingsForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Company company = companyService.getCompanyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(company);
        return "company/settings/company";
    }

    @PostMapping("/company/publish")
    public String companyPublish(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        companyService.publish(company);
        redirectAttributes.addFlashAttribute("message", "모집공고를 공개하였습니다.");
        return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
    }

    @PostMapping("/company/close")
    public String companyClose(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        companyService.close(company);
        redirectAttributes.addFlashAttribute("message", "모집공고를 종료했습니다.");
        return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
    }

    @PostMapping("/recruit/start")
    public String companyRecruitStart(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        if (!company.canUpdateRecruiting()) {
            redirectAttributes.addFlashAttribute("message", "1시간 안에 지원 공고 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
        }
        companyService.startRecruit(company);
        return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
    }

    @PostMapping("/recruit/stop")
    public String companyRecruitStop(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        if (!company.canUpdateRecruiting()) {
            redirectAttributes.addFlashAttribute("message", "현재 인원 모집중이 아닙니다.");
            return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
        }
        companyService.stopRecruit(company);
        return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
    }

    @PostMapping("/company/path")
    public String updateCompanyPath(@CurrentAccount Account account, @PathVariable String path, @RequestParam String newPath, Model model, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        if (!companyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(company);
            redirectAttributes.addFlashAttribute("companyPathError", "회사 경로를 바꾸는데 실패하였습니다.");
            return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
        }
        companyService.updateCompanyPath(company,newPath);
        redirectAttributes.addFlashAttribute("message", "회사 경로를 변경하였습니다.");
        return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
    }

    @PostMapping("/company/name")
    public String updateCompanyName(@CurrentAccount Account account, @PathVariable String path, @RequestParam String newName, Model model, RedirectAttributes redirectAttributes) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        if (!companyService.isValidName(newName)) {
            model.addAttribute(account);
            model.addAttribute(company);
            redirectAttributes.addFlashAttribute("companyNameError","회사 이름을 바꾸는데 실패하였습니다.");
            return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
        }
        companyService.updateCompanyName(company,newName);
        redirectAttributes.addFlashAttribute("message","회사 이름을 변경하였습니다.");
        return "redirect:/company/" + company.getEncodedPath() + "/settings/company";
    }

    @PostMapping("/company/remove")
    public String removeCompany(@CurrentAccount Account account, @PathVariable String path) {
        Company company = companyService.getCompanyToUpdateStatus(account, path);
        companyService.removeCompany(company);
        return "redirect:/";
    }
}
