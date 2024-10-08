package com.project.api.metting.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.api.metting.entity.Gender;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {

    private String email;
    private String password;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;
    private String phoneNumber; // 폰 번호
    private String univName;
    private String major; // 전공
    private Gender gender; // 성별
    private String nickname;
}
