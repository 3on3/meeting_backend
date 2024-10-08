package com.project.api.metting.repository;

import com.project.api.metting.entity.Group;
import com.project.api.metting.entity.GroupMatchingHistory;
import com.project.api.metting.entity.GroupProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface GroupMatchingHistoriesRepository extends JpaRepository<GroupMatchingHistory, String> {



    boolean existsByResponseGroupAndRequestGroup(Group responseGroup, Group requestGroup);
    GroupMatchingHistory findByResponseGroupAndRequestGroup(Group responseGroup, Group requestGroup);

    GroupMatchingHistory findByResponseGroupAndProcess(Group group, GroupProcess process);
    boolean existsByResponseGroupAndProcess(Group group, GroupProcess process);
    boolean existsByRequestGroupAndProcess(Group group, GroupProcess process);

    GroupMatchingHistory findByRequestGroupAndProcess(Group group, GroupProcess process);

    List<GroupMatchingHistory> findAllByRequestGroup(Group requestGroup);
    List<GroupMatchingHistory> findAllByResponseGroup(Group responseGroup);
    List<GroupMatchingHistory> findByRequestGroupOrResponseGroup(Group requestGroup, Group responseGroup);

    List<GroupMatchingHistory> findAllByResponseGroupAndProcess(Group group, GroupProcess groupProcess);
}
