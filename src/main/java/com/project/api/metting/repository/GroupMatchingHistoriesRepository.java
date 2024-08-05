package com.project.api.metting.repository;

import com.project.api.metting.entity.Group;
import com.project.api.metting.entity.GroupMatchingHistory;
import com.project.api.metting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface GroupMatchingHistoriesRepository extends JpaRepository<GroupMatchingHistory, String> {


    boolean existByResponseGroupAndRequestGroup(Group responseGroup, Group requestGroup);
    boolean existByResponseGroupAndRequestGroupAndProcess(Group responseGroup, Group requestGroup, Process process);

}
