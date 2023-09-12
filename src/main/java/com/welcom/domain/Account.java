package com.welcom.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private boolean emailVerified; //이메일 인증절차= 옳은 계정인지
    private String emailCheckToken; //이메일 검증시 토큰값 비교를 위함
    private LocalDateTime joinedAt;
    private String bio;
    private String url;
    private String occupation;
    private String location;
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;
    private boolean company;
    private boolean companyCreatedByEmail;
    private boolean companyCreatedByWeb;
    private boolean companyRecruitResultByEmail;
    private boolean companyRecruitResultByWeb;
    private boolean companyUpdatedByEmail;
    private boolean companyUpdatedByWeb;
}
