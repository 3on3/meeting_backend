package com.project.api.metting.service;

import com.project.api.metting.dto.request.UserRegisterDto;
import com.project.api.metting.entity.Membership;
import com.project.api.metting.entity.User;
import com.project.api.metting.entity.UserMembership;
import com.project.api.metting.entity.UserProfile;
import com.project.api.metting.repository.UserRepository;

import com.univcert.api.UnivCert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Service
public class UserSignUpService {

    @Value("${univcert.api.key}")
    private String univCertApiKey;

    private final UserRepository userRepository; // 사용자 DB

    private final PasswordEncoder encoder; // 비밀번호 암호화

    // 이메일 중복 확인 및 사용자 처리
    public boolean checkEmailDuplicate(String email, String univName) {
        // 이메일이 이미 존재하는지 확인 (이메일이 데이터베이스에 존재하는지 여부를 확인)
        boolean exists = userRepository.existsByEmail(email);
        log.info("Checking email {} is duplicate: {}", email, exists);

        if (exists) {

            // 사용자를 데이터베이스에서 조회 (이메일을 통해 사용자 정보 가져오기)
            User user = userRepository.findByEmail(email).orElseThrow();

            // 탈퇴한 회원인지 확인
            if (user.getIsWithdrawn()) {
                log.info("The email {} belongs to a withdrawn user.", email);
                return true; // 탈퇴한 회원이므로 중복된 이메일로 처리
            }

            // 이메일이 이미 존재하지만, 사용자가 인증을 완료하지 않은 경우
            if (notFinish(email)) {
//                // 사용자를 데이터베이스에서 조회 (이메일을 통해 사용자 정보 가져오기)
//                User user = userRepository.findByEmail(email).orElseThrow();
                // 이전 인증 기록을 삭제하고 새로운 인증 코드를 재발송
                clearAndResendVerification(email, user.getUnivName());
                return false; // 인증 코드 재발송 후 false를 반환하여 중복된 이메일이 아님을 알림
            }
            return true; // 사용자가 존재하고, 인증이 완료된 경우 true를 반환하여 중복된 이메일임을 알림
        } else {
            // 이메일이 존재하지 않으면 신규 사용자 등록 처리
            processSignUp(email, univName);
            return false; // 신규 등록이므로 false를 반환하여 중복된 이메일이 아님을 알림
        }
    }

    private void clearAndResendVerification(String email, String univName) {
        User user = userRepository.findByEmail(email).orElseThrow();

        // Check if the user is already verified
        if (user.getIsVerification()) {
            log.info("User with email {} is already verified. No need to resend verification email.", email);
            return;
        }

        try {
            Map<String, Object> clearResponse = UnivCert.clear(univCertApiKey, email);
            if (clearResponse != null && Boolean.TRUE.equals(clearResponse.get("success"))) {
                log.info("Cleared previous verification code for email: {}", email);

                // Clear 성공 후 새로운 인증 메일 발송
                Map<String, Object> certifyResponse = UnivCert.certify(univCertApiKey, email, univName, false);
                if (certifyResponse != null && Boolean.TRUE.equals(certifyResponse.get("success"))) {
                    log.info("Resent verification email to: {}", email);
                } else {
                    log.error("Failed to resend verification email to: {}", email);
                }
            } else {
                log.error("Failed to clear verification code for email: {}", email);
            }
        } catch (IOException e) {
            log.error("Exception while clearing verification code and resending: ", e);
        }
    }

    // 사용자가 인증을 완료했는지 확인
    private boolean notFinish(String email) {
        return userRepository.findByEmail(email)
                .map(user -> !user.getIsVerification() || user.getPassword() == null)
                .orElse(false);
    }

    // 인증코드 초기화 요청
    private void clearVerificationCode(String email) {
        try {
            Map<String, Object> response = UnivCert.clear(univCertApiKey, email);
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                log.info("Cleared previous verification code for email: {}", email);
            } else {
                log.error("Failed to clear verification code for email: {}", email);
            }
        } catch (IOException e) {
            log.error("Exception while clearing verification code: ", e);
        }
    }

    // 사용자 등록 처리
    @Transactional
    public void processSignUp(String email, String univName) {
        try {
            // 인증 코드 초기화 (사용자 존재 여부와 관계없이 항상 수행)
            clearVerificationCode(email);

            // 기존 사용자 삭제 (존재하는 경우)
            userRepository.findByEmail(email).ifPresent(user -> {
                log.info("Existing user with email {} found. Deleting user.", email);
                userRepository.delete(user);
            });

            // 인증 메일 발송
            Map<String, Object> response = UnivCert.certify(univCertApiKey, email, univName, false);
            log.info("UnivCert response: {}", response);

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                User newUser = User.builder()
                        .email(email)
                        .univName(univName)
                        .isVerification(false)
                        .build();
                userRepository.save(newUser);
                log.info("New user with email {} successfully created and saved.", email);
            } else {
                log.error("Failed to send verification email to {}", email);
                throw new RuntimeException("Failed to send verification email");
            }
        } catch (IOException e) {
            log.error("Exception while sending verification request: ", e);
            throw new RuntimeException("Failed to process sign up", e);
        }
    }

    // 닉네임 중복 확인
    public boolean checkNicknameDuplicate(String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        log.info("Checking nickname {} is duplicate: {}", nickname, exists);
        return exists;
    }

    // 전화번호 중복 확인
    public boolean checkPhoneNumberDuplicate(String phoneNumber) {
        boolean exists = userRepository.existsByPhoneNumber(phoneNumber);
        log.info("Checking phone number {} is duplicate: {}", phoneNumber, exists);
        return exists;
    }

    // 인증 코드 확인
    public boolean verifyCode(String email, int code) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                log.error("User not found for email: {}", email);
                return false;
            }

            String univName = user.getUnivName();
            if (univName == null) {
                log.error("University name not found for user: {}", email);
                return false;
            }

            // 인증 코드 확인
            Map<String, Object> response = UnivCert.certifyCode(univCertApiKey, email, univName, code);

            // 서버 응답 확인
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                log.info("Verification succeeded for email: {}", email);
                user.setIsVerification(true);
                userRepository.save(user);
                return true;  // 인증 성공
            } else {
                String message = response != null ? (String) response.get("message") : "Unknown error occurred";
                log.error("Verification failed for email: {}. Reason: {}", email, message);
                return false;  // 인증 실패
            }
        } catch (IOException e) {
            log.error("Exception while verifying code: ", e);
            return false;
        }
    }

    // 회원가입 마무리
    @Transactional
    public void confirmSignUp(UserRegisterDto dto) {
        User findUser = userRepository.findByEmail(dto.getEmail()).orElseThrow(() ->
                new IllegalArgumentException("User not found with email: " + dto.getEmail()));

        log.info("Confirm sign up : {}", dto);

        // 인증 코드 초기화 (회원가입 마무리 처리 시)
        clearVerificationCode(dto.getEmail());

        String password = dto.getPassword();
        String encodedPassword = encoder.encode(password); // 비밀번호 암호화

        findUser.confirm(
                encodedPassword,
                dto.getName(),
                dto.getBirthDate(),
                dto.getPhoneNumber(),
                dto.getUnivName(),
                dto.getMajor(),
                dto.getGender(),
                dto.getNickname()
        );

        // UserProfile 생성 및 설정
        UserProfile userProfile = UserProfile.builder()
                .profileImg("https://spring-file-bucket-yocong.s3.ap-northeast-2.amazonaws.com/2024/default_profile.png")
                .profileIntroduce(null)
                .user(findUser)
                .build();

        findUser.setUserProfile(userProfile);

        // UserMembership 생성 및 설정
        UserMembership userMembership = UserMembership.builder()
                .user(findUser)
                .auth(Membership.GENERAL) // 기본 멤버십으로 GENERAL 설정
                .build();

        // User와 UserMembership 연관 설정
        findUser.setMembership(userMembership);

        userRepository.save(findUser);
    }

    // 닉네임 업데이트
    public void updateNickname(String email, String nickname) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setNickname(nickname);
        userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        // 이메일로 사용자를 검색
        return userRepository.findByEmail(email).orElse(null);
    }

}