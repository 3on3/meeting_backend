package com.project.api.metting.dto.request;

import com.project.api.metting.entity.ChatMessage;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class ChatMessageRequestDto {
    private String userId;
    private String userEmail;
    private String userNickname;
    private String messageId;
    private String messageContent;
    private String profileImg;
    private LocalDateTime messageAt;


     public ChatMessageRequestDto(ChatMessage chatMessage) {
         this.userEmail = chatMessage.getUser().getEmail();
         this.userId = chatMessage.getUser().getId();
         this.userNickname = chatMessage.getUser().getNickname();
         this.messageId = chatMessage.getId();
         this.messageContent = chatMessage.getMessageContent();
         this.profileImg = chatMessage.getUser().getUserProfile().getProfileImg();
         this.messageAt = chatMessage.getCreatedAt();
     }
}
