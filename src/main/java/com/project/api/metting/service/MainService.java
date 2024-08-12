package com.project.api.metting.service;


import com.project.api.metting.dto.request.MainMeetingListFilterDto;
import com.project.api.metting.dto.response.MainMeetingListResponseDto;
import com.project.api.metting.entity.Group;
import com.project.api.metting.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MainService {
    public final GroupRepository groupRepository;


//    Group 전체 조회
    public List<MainMeetingListResponseDto> getMeetingList() {
//        List<Group> MeetingList = groupRepository.findAll();
//        log.info("MeetingList: {}", MeetingList);

        List<MainMeetingListResponseDto> groupUsersByAllGroup = groupRepository.findGroupUsersByAllGroup();
        return groupUsersByAllGroup;
    }

//    group 필터링
    public Page<MainMeetingListResponseDto> postMeetingList(MainMeetingListFilterDto dto) {

        return  groupRepository.filterGroupUsersByAllGroup(dto);
    }

//    나이 계산하기

}
