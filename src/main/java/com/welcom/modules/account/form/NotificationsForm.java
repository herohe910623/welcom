package com.welcom.modules.account.form;

import lombok.Data;

@Data
public class NotificationsForm {

    private boolean companyCreatedByEmail;      // 회사 생성 알림
    private boolean companyCreatedByWeb;
    private boolean companyRecruitResultByEmail;        // 회사 지원 결과 알림
    private boolean companyRecruitResultByWeb;
    private boolean companyUpdatedByEmail;      //회사 수정 알림
    private boolean companyUpdatedByWeb;
}
