package com.project.api.metting.controller;

import com.project.api.auth.TokenProvider;
import com.project.api.auth.TokenProvider.TokenUserInfo;
import com.project.api.metting.dto.request.AlarmRequestDto;
import com.project.api.metting.dto.request.GroupMatchingRequestDto;
import com.project.api.metting.dto.request.GroupRequestDto;
import com.project.api.metting.dto.response.GroupMatchingAlarmResponseDto;
import com.project.api.metting.dto.response.GroupMatchingResponseDto;
import com.project.api.metting.entity.GroupProcess;
import com.project.api.metting.entity.User;
import com.project.api.metting.service.GroupMatchingService;
import com.project.api.metting.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group/matching")
@RequiredArgsConstructor
@Slf4j
public class GroupMatchingController {
    private final GroupMatchingService groupMatchingService;
    private final GroupService groupService;

    /**
     * 매칭 요청(신청자 역할)
     * @param groupMatchingRequestDto - 히스토리 정보에 추가될 리퀘스트 정보 dto
     * @return - 요청 처리 body
     */
    @PostMapping("/createRequest")
    public ResponseEntity<?> createRequest(@AuthenticationPrincipal TokenUserInfo tokenUserInfo,@RequestBody GroupMatchingRequestDto groupMatchingRequestDto) {
        // 1. 히스토리 생성 요청
        groupMatchingService.createHistory(groupMatchingRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("매칭 신청이 성공적으로 생성되었습니다.");
    }

    /**
     * 매칭 요청 리스트 열람(주최자 기준)
     *
     * @param groupId - 주최자 그룹 아이디
     * @return - 신청자 리스트 반환
     */
    @GetMapping("/response")
    @Transactional(readOnly = true)
    public ResponseEntity<List<GroupRequestDto>> matchingResponse(@AuthenticationPrincipal TokenUserInfo tokenUserInfo, @RequestParam String groupId) {
        List<GroupRequestDto> groupResponseDtoList = groupMatchingService.viewRequestList(groupId);

        return ResponseEntity.ok().body(groupResponseDtoList);
    }

    /**
     * 요청 수락 요청
     * @param groupMatchingResponseDto - 수락할 히스토리 아이디
     * @return - 성공 메세지
     */
    @PostMapping("/response-accept")
    public ResponseEntity<?> responseAccept(@AuthenticationPrincipal TokenUserInfo tokenUserInfo,@RequestBody GroupMatchingResponseDto groupMatchingResponseDto) {
        GroupProcess groupProcess = groupMatchingService.acceptRequest(groupMatchingResponseDto);

        return ResponseEntity.ok().body(groupProcess);
    }

    /**
     * 요청 거절 요청
     * @param groupMatchingResponseDto - 거절할 히스토리 아이디
     * @return - 실패 메세지
     */
    @PostMapping("/response-deny")
    public ResponseEntity<?> responseDeny(@AuthenticationPrincipal TokenUserInfo tokenUserInfo,@RequestBody GroupMatchingResponseDto groupMatchingResponseDto) {
        GroupProcess groupProcess = groupMatchingService.denyRequest(groupMatchingResponseDto);

        return ResponseEntity.ok().body(groupProcess);
    }

    @PostMapping("/alarm")
    public ResponseEntity<?> alarm(@RequestBody GroupMatchingAlarmResponseDto groupMatchingAlarmResponseDto) {

        User hostUser = groupService.findByGroupHost(groupMatchingAlarmResponseDto.getResponseGroupId());

        if(hostUser == null) {
            return ResponseEntity.badRequest().body("존재하지 않는 그룹입니다.");
        }

        AlarmRequestDto alarmRequestDto = new AlarmRequestDto();
        alarmRequestDto.setEmail(hostUser.getEmail());
        return ResponseEntity.ok().body(alarmRequestDto);
    }
}