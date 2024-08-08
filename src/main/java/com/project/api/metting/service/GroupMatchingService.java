package com.project.api.metting.service;



import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import com.project.api.exception.GroupMatchingFailException;
import com.project.api.metting.dto.request.GroupMatchingRequestDto;
import com.project.api.metting.dto.response.GroupResponseDto;
import com.project.api.metting.entity.Group;
import com.project.api.metting.entity.GroupMatchingHistory;
import com.project.api.metting.entity.GroupProcess;
import com.project.api.metting.repository.GroupMatchingHistoriesCustomImpl;
import com.project.api.metting.repository.GroupMatchingHistoriesRepository;
import com.project.api.metting.repository.GroupRepository;
import com.project.api.metting.repository.GroupRepositoryCustomImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMatchingService {

    private  final GroupMatchingHistoriesRepository groupMatchingHistoriesRepository;
    private  final GroupRepository groupRepository;
    private final GroupMatchingHistoriesCustomImpl groupMatchingHistoriesCustomImpl;
    private final GroupRepositoryCustomImpl groupRepositoryCustomImpl;

    /**
     * 그룹 - 그룹 채팅 신청 버튼 클릭시 히스토리 생성하는 함수
     * @param groupMatchingRequestDto - 신청자 그룹, 주최자 그룹 이름 정보
     */
    public void createHistory (GroupMatchingRequestDto groupMatchingRequestDto){
        try {
            // 신청자 그룹
            Group requestGroup = groupRepository.findById(groupMatchingRequestDto.getRequestGroupId()).orElse(null);
            // 주최자 그룹
            Group responseGroup = groupRepository.findById(groupMatchingRequestDto.getResponseGroupId()).orElse(null);

            // [신청 불가 요건]
            // 1. 이미 매칭 신청 중인 경우, 2. 매칭 히스토리 denied 상태일 경우
            boolean exists = groupMatchingHistoriesRepository.existsByResponseGroupAndRequestGroup(responseGroup, requestGroup)
                    || groupMatchingHistoriesRepository.existsByResponseGroupAndRequestGroup(requestGroup, responseGroup);
            if(exists){
                throw new GroupMatchingFailException("이미 신청한 그룹입니다.", HttpStatus.CONFLICT);
            }
            // 3. 인원 수 다를 경우
            if(requestGroup.getMaxNum() != responseGroup.getMaxNum()){
                throw new GroupMatchingFailException("인원 수가 다릅니다.", HttpStatus.BAD_REQUEST);
            }
            // 4. 지역 다를 경우
            if(!requestGroup.getGroupPlace().equals(responseGroup.getGroupPlace()) ){
                throw new GroupMatchingFailException("희망지역이 다릅니다.", HttpStatus.BAD_REQUEST);
            }

            // 히스토리 생성
            GroupMatchingHistory groupMatchingHistory = GroupMatchingHistory.builder()
                    .requestGroup(requestGroup)
                    .responseGroup(responseGroup)
                    .build();

            // 히스토리 저장
            groupMatchingHistoriesRepository.save(groupMatchingHistory);
        } catch (Exception e){
            throw new GroupMatchingFailException("예외 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }




    /**
     * 매칭 수락 또는 거절 프로세싱
     * @param groupid - 수정할 히스토리의 아이디
     * @param process - 프로세스 상태 INVITED, MATCHED, DENIED
     * @param message - 예외처리 메세지
     */
    private GroupProcess processingRequest (String groupid, GroupProcess process, String message){
        try{
            List<GroupMatchingHistory> histories = groupMatchingHistoriesCustomImpl.findByResponseGroupId(groupid);

            String historyId = histories.get(0).getId();;
            GroupMatchingHistory matchingHistory = groupMatchingHistoriesRepository.findById(historyId).orElse(null);

            if(matchingHistory.getProcess().equals(process)){
                throw new GroupMatchingFailException(message, HttpStatus.BAD_REQUEST);
            }
            matchingHistory.setProcess(process);
            groupMatchingHistoriesRepository.save(matchingHistory);
            return process;

        } catch (NullPointerException e){
            throw new GroupMatchingFailException("히스토리에 일치하는 groupId 가 없습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e){
            throw new GroupMatchingFailException("예외 발생: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 매칭 수락
     * - 히스토리 process 컬럼을 invited -> matched 로 수정하는 함수
     * @param groupId - 수정할 히스토리의 아이디
     */
    public GroupProcess acceptRequest (String groupId){
        String message = "이미 매칭된 그룹입니다.";
        GroupProcess process = processingRequest(groupId, GroupProcess.MATCHED, message);
        return process;
    }

    /**
     * 매칭 거절
     * - 히스토리 process 컬럼을 invited -> denied 로 수정하는 함수
     * @param groupId - 수정할 히스토리의 아이디
     */
    public GroupProcess denyRequest (String groupId){
        String message = "이미 매칭 거절된 그룹입니다.";
        GroupProcess process = processingRequest(groupId, GroupProcess.DENIED, message);
        return process;
    }

    /**
     * 주최자 기준 신청자 그룹 리스트 조회 함수
     * @param groupId - 주최자 그룹 아이디
     * @return - 신청자 그룹 리스트 Dto 반환
     */
    @Transactional(readOnly = true)
    public List<GroupResponseDto> viewRequestList(String groupId) {

        List<GroupMatchingHistory> histories = groupMatchingHistoriesCustomImpl.findByResponseGroupId(groupId);
        List<GroupMatchingHistory> collect = histories.stream()
                .filter(groupMatchingHistory -> groupMatchingHistory.getProcess().equals(GroupProcess.INVITING))
                .collect(Collectors.toList());
        return collect.stream()
                .map(GroupMatchingHistory::getResponseGroup)
                .map(groupRepositoryCustomImpl::convertToGroupResponseDto)
                .collect(Collectors.toList());

    }
}
