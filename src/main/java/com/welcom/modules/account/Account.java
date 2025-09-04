package com.welcom.modules.account;

import com.welcom.modules.company.Company;
import com.welcom.modules.zone.Zone;
import com.welcom.modules.tag.Tag;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String nickname;
    private String password;
    private LocalDateTime emailTokenGeneratedAt; // emailToken 생성된 시간
    private boolean emailVerified; //이메일 인증절차= 옳은 계정인지
    private String emailCheckToken; //이메일 검증시 토큰값 비교를 위함
    private LocalDateTime joinedAt; // 가입한 시간
    private String bio;     // 회사
    private String url;     // 링크
    private String occupation; // 직군
    private String location;    // 위치
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;
    private boolean company;
    private boolean companyCreatedByEmail;      // 회사 생성 알림
    private boolean companyCreatedByWeb = true;
    private boolean companyRecruitResultByEmail;        // 회사 지원 결과 알림
    private boolean companyRecruitResultByWeb = true;
    private boolean companyUpdatedByEmail;      //회사 수정 알림
    private boolean companyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();
    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = String.valueOf(UUID.randomUUID());
        this.emailTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean canSendConfirmEmail() {
        return emailTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public boolean isValidToken(String token) {
        return this.getEmailCheckToken().equals(token);
    }

//    public boolean isManagerOf(Company company) {
//        return company.getManagers().contains(this);
//    }

}
