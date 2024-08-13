package com.project.api.metting.repository;

import com.project.api.metting.dto.request.MainMeetingListFilterDto;
import com.project.api.metting.dto.request.MyChatListRequestDto;
import com.project.api.metting.dto.response.GroupResponseDto;
import com.project.api.metting.dto.response.MainMeetingListResponseDto;
import com.project.api.metting.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GroupRepositoryCustom {

    //    main meetingList DTO
    Page<MainMeetingListResponseDto> filterGroupUsersByAllGroup(MainMeetingListFilterDto dto);

    List<GroupResponseDto> findGroupsByUserEmail(String email);
    List<Group> findGroupsEntityByUserEmail(String email);

    Page<MainMeetingListResponseDto> findGroupUsersByAllGroup(String email,Pageable pageable);
    public void myChatListRequestDto(Group group, MyChatListRequestDto dto);
}
