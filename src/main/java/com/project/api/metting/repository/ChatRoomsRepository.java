package com.project.api.metting.repository;

import com.project.api.metting.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomsRepository extends JpaRepository<ChatRoom, String> {

   Optional<ChatRoom> findById(String roomId);
}
