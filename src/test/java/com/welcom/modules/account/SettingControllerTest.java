package com.welcom.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.welcom.modules.tag.Tag;
import com.welcom.modules.zone.Zone;
import com.welcom.modules.tag.TagForm;
import com.welcom.modules.zone.ZoneForm;
import com.welcom.modules.tag.TagRepository;
import com.welcom.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    ZoneRepository zoneRepository;
    @Autowired
    AccountService accountService;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("herohe")
    @Test
    @DisplayName("프로필 수정 페이지 이동")
    public void updateProfileForm() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"));
    }

    // 유저가 들어가 있어야 수정 가능하다. 여기선 @WithMockUser 가 사용 불가능하다 DB에 값을 안넣기때문에
    @WithAccount("herohe")
    @DisplayName("프로필 수정 테스트 성공")
    @Test
    public void updateProfile_success() throws Exception {
        String bio = "짧은 소개";
        mockMvc.perform(post("/settings/profile")
                        .with(csrf())
                        .param("bio", bio))
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(status().is3xxRedirection());

        Account account = accountRepository.findByNickname("herohe");
        assertEquals(account.getBio(), bio);
    }

    @WithAccount("herohe")
    @DisplayName("프로필 수정 테스트 실패 - 너무 긴 bio")
    @Test
    public void updateProfile_fail() throws Exception {
        String bio = "테스트 실패를 위해 엄청 길게 작성중 입니다. 복사 시작합니다.테스트 실패를 위해 엄청 길게 작성중 입니다. 복사 시작합니다." +
                "테스트 실패를 위해 엄청 길게 작성중 입니다. 복사 시작합니다.테스트 실패를 위해 엄청 길게 작성중 입니다." +
                " 복사 시작합니다.테스트 실패를 위해 엄청 길게 작성중 입니다. 복사 시작합니다.테스트 실패를 위해 엄청 길게 작성중 입니다. 복사 시작합니다" +
                "테스트 실패를 위해 엄청 길게 작성중 입니다. ";
        mockMvc.perform(post("/settings/profile")
                        .with(csrf())
                        .param("bio", bio))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(view().name("settings/profile"));

        Account account = accountRepository.findByNickname("herohe");
        assertNull(account.getBio());
    }

    @WithAccount("herohe")
    @DisplayName("패스워드 수정 폼 이동")
    @Test
    public void updatePasswordForm() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"));
    }

    @WithAccount("herohe")
    @DisplayName("패스워드 수정 - 실패(패스워드 확인다름)")
    @Test
    public void updatePassword_fail_newAndConfirm() throws Exception {
        mockMvc.perform(post("/settings/password")
                        .with(csrf())
                        .param("newPassword", "123456789")
                        .param("newPasswordConfirm", "12345678910"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name("settings/password"));
    }

    @WithAccount("herohe")
    @DisplayName("패스워드 수정 - 성공")
    @Test
    public void updatePassword_success() throws Exception {
        mockMvc.perform(post("/settings/password")
                        .with(csrf())
                        .param("newPassword", "12345678910")
                        .param("newPasswordConfirm", "12345678910"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("herohe");
        assertTrue(passwordEncoder.matches("12345678910", account.getPassword()));
    }

    @WithAccount("herohe")
    @DisplayName("알림 설정 폼 이동")
    @Test
    public void updateNotificationsForm() throws Exception {
        mockMvc.perform(get("/settings/notifications"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationsForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/notifications"));
    }

    @WithAccount("herohe")
    @DisplayName("알림 설정 성공")
    @Test
    public void updateNotifications_success() throws Exception {
        mockMvc.perform(post("/settings/notifications")
                                .with(csrf())
                                .param("companyCreatedByWeb", "true")
                                .param("companyCreatedByEmail", "false")
                                .param("companyRecruitResultByWeb", "false")
                                .param("companyRecruitResultByEmail", "true")
                                .param("companyUpdatedByWeb", "false")
//                        .param("companyUpdatedByEmail","false")
                )
                .andExpect(flash().attributeExists("message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notifications"));

        Account account = accountRepository.findByNickname("herohe");
        assertFalse(account.isCompanyUpdatedByEmail());
        assertTrue(account.isCompanyCreatedByWeb());
    }

    @WithAccount("herohe")
    @DisplayName("계정 프로필 이름 변경 폼 이동")
    @Test
    public void updateAccountForm() throws Exception {
        mockMvc.perform(get("/settings/account"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"));
    }

    @WithAccount("herohe")
    @DisplayName("계정 프로필 이름 변경 성공")
    @Test
    public void updateAccount_success() throws Exception {
        mockMvc.perform(post("/settings/account")
                        .with(csrf())
                        .param("nickname", "gijin")
                )
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/account"))
                .andExpect(status().is3xxRedirection());

        Account account = accountRepository.findByNickname("gijin");
        assertNotNull(account);
    }

    @WithAccount("herohe")
    @DisplayName("계정 프로필 이름 변경 실패")
    @Test
    public void updateAccount_fail() throws Exception {
        mockMvc.perform(post("/settings/account")
                        .with(csrf())
                        .param("nickname", "x")
                )
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(view().name("settings/account"));
        Account account = accountRepository.findByNickname("x");
        assertNull(account);
    }

    @WithAccount("herohe")
    @DisplayName("계정 프로필 태그 폼 이동")
    @Test
    public void updateTagsForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/tags"));
    }

    @WithAccount("herohe")
    @DisplayName("계정 프로필 태그 추가")
    @Test
    public void addTags() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf())
                )
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("herohe").getTags().contains(newTag));
    }

    @WithAccount("herohe")
    @DisplayName("계정 프로필 태그 삭제")
    @Test
    public void removeTags() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(herohe, newTag);

        assertTrue(herohe.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf())
                )
                .andExpect(status().isOk());

        assertFalse(herohe.getTags().contains(newTag));
    }

    @WithAccount("herohe")
    @DisplayName("계정 주요 활동 지역 폼 이동")
    @Test
    public void updateZonesForm() throws Exception {
        mockMvc.perform(get("/settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/zones"));
    }

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @WithAccount("herohe")
    @DisplayName("계정 주요 활동 지역 추가")
    @Test
    public void addZones() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/settings/zones/add")
                        .with(csrf()).
                        contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                )
                .andExpect(status().isOk());

        Account herohe = accountRepository.findByNickname("herohe");
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        assertTrue(herohe.getZones().contains(zone));
    }

    @WithAccount("herohe")
    @DisplayName("계정 주요 활동 지역 제거")
    @Test
    public void removeZones() throws Exception {

        Account herohe = accountRepository.findByNickname("herohe");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(herohe, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/settings/zones/remove")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                )
                .andExpect(status().isOk());

        assertFalse(herohe.getZones().contains(zone));
    }
}