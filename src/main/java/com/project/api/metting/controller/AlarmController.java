package com.project.api.metting.controller;

import com.project.api.auth.TokenProvider;
import com.project.api.metting.dto.request.AlarmListRequestDto;
import com.project.api.metting.entity.Alarm;
import com.project.api.metting.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping
    public ResponseEntity<?> findAlarmList(@AuthenticationPrincipal TokenProvider.TokenUserInfo tokenUserInfo) {

        String loginUserId = tokenUserInfo.getUserId();

        List<AlarmListRequestDto> alarmList = alarmService.findAlarmList(loginUserId);

        if(alarmList == null || alarmList.isEmpty()) {
            alarmList = new ArrayList<>();
        }

        return ResponseEntity.ok().body(alarmList);
    }
}
