package com.project.api.metting.service;

import com.project.api.metting.dto.response.UserMyPageDto;
import com.project.api.metting.entity.User;
import com.project.api.metting.entity.UserProfile;
import com.project.api.metting.repository.UserMyPageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserMyPageServiceTest {

    @Mock
    private UserMyPageRepository userMyPageRepository;

    @InjectMocks
    private UserMyPageService userMyPageService;

    @Test
    void testGetUserInfo() {
        // Given
        String userId = "1";
        UserProfile userProfile = UserProfile.builder()
                .profileImg("프사")
                .profileIntroduce("안뇽")
                .build();

        User user = User.builder()
                .id(userId)
                .password("1234")
                .birthDate(new Date())
                .phoneNumber("010-123-4567")
                .major("컴공")
                .nickname("최강미녀")
                .userProfile(userProfile)
                .build();

        userProfile.setUser(user);

        when(userMyPageRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<UserMyPageDto> result = userMyPageService.getUserInfo(userId);

        // Then
        assertEquals(true, result.isPresent());
        assertEquals("1234", result.get().getPassword());
        assertEquals("010-123-4567", result.get().getPhoneNumber());
        assertEquals("컴공", result.get().getMajor());
        assertEquals("최강미녀", result.get().getNickname());
        assertEquals("프사", result.get().getProfileImg());
        assertEquals("안뇽", result.get().getProfileIntroduce());
    }

    @Test
    void testGetUserInfo_UserNotFound() {
        // Given
        String userId = "non-existing-user-id";

        // Mocking repository response
        when(userMyPageRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<UserMyPageDto> result = userMyPageService.getUserInfo(userId);

        // Then
        assertEquals(false, result.isPresent());
    }
}
