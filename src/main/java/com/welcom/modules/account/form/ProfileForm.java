package com.welcom.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;

@Data
public class ProfileForm {

    @Length(max = 35)
    private String bio;
    @Length(max = 50)
    private String url;
    @Length(max = 50)
    private String occupation;
    @Length(max = 50)
    private String location;
    private String profileImage;
}
