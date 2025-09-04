package com.welcom.modules.zone;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Zone {

    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String city;    // 영문 도시 이름
    @Column(nullable = false)
    private String localNameOfCity; // 한국어 도시 이름
    @Column(nullable = true)
    private String province;    // 주 이름,nullable

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city,localNameOfCity,province);
    }
}
