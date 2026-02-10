package com.redflag.redflag.user.controller;

import com.redflag.redflag.user.dto.UserResponse;
import com.redflag.redflag.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;
    private static final String USER_COOKIE_NAME = "user_id";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30; // 30일

    /**
     * 사용자 ID 발급 (쿠키 생성)
     * - 쿠키가 없으면 새로 생성
     * - 쿠키가 있으면 기존 사용자 정보 반환
     */
    @PostMapping("/issue")
    @Operation(
            summary = "사용자 ID 발급", security = {},
            description = "UUID 기반 사용자 ID를 생성하고 쿠키에 저장합니다.\n\n" +
                    "**Swagger 테스트 방법:**\n" +
                    "1. 이 API를 실행하세요.\n" +
                    "2. 응답의 'userId' 값을 복사하세요.\n" +
                    "3. 다른 API 테스트 시 'Cookie' 파라미터에 다음 형식으로 입력:\n" +
                    "   `user_id=복사한UUID`\n\n" +
                    "**주의:** Swagger는 Set-Cookie를 자동으로 처리하지 않으므로 수동 입력이 필요합니다."
    )
    public ResponseEntity<UserResponse> issueUserId(
            @CookieValue(name = USER_COOKIE_NAME, required = false) String existingId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 기존 쿠키가 유효한지 확인
        if (existingId != null) {
            try {
                UUID uuid = UUID.fromString(existingId);
                if (userService.existsUser(uuid)) {
                    return ResponseEntity.ok(UserResponse.of(uuid));
                }
            } catch (IllegalArgumentException ignored) {}
        }

        UUID newUserId = userService.createUser();
        response.addCookie(createUserCookie(newUserId, request));

        return ResponseEntity.ok(UserResponse.of(newUserId));
    }

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    @Operation(
            summary = "현재 사용자 조회", 
            description = "쿠키의 UUID로 현재 사용자 정보를 조회합니다.\n\n" +
                    "**Swagger 테스트 방법:**\n" +
                    "1. 먼저 `/api/user/issue`를 호출하여 userId를 발급받으세요.\n" +
                    "2. 응답의 'userId'를 복사하세요.\n" +
                    "3. 아래 'Cookie' 파라미터에 `user_id=복사한UUID` 형식으로 입력하세요."
    )
    public ResponseEntity<UserResponse> getCurrentUser(
            @CookieValue(name = USER_COOKIE_NAME, required = false) String userId
    ){
        if (userId == null) {
            return ResponseEntity.status(401).body(new UserResponse(null, "쿠키가 없습니다."));
        }

        UUID userUuid = UUID.fromString(userId);

        if (!userService.existsUser(userUuid)) {
            return ResponseEntity.status(404).body(
                    new UserResponse(userUuid, "사용자를 찾을 수 없습니다.")
            );
        }

        return ResponseEntity.ok(UserResponse.of(userUuid));
    }

    /**
     * 쿠키 생성 헬퍼 메서드
     */
    private Cookie createUserCookie(UUID userId, HttpServletRequest request) { // request 파라미터 추가
        Cookie cookie = new Cookie(USER_COOKIE_NAME, userId.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);

        // HTTPS인 경우에만 Secure 설정
        // 로컬(HTTP)에서는 false, 배포(HTTPS)에서는 true로 자동 전환됩니다.
        boolean isHttps = request.isSecure();
        cookie.setSecure(isHttps);

        // SameSite 설정
        if (isHttps) {
            // 배포 환경: 프론트와 백엔드 도메인이 다를 경우를 대비해 None 설정
            // SameSite=None은 반드시 Secure=true와 함께 써야 합니다.
            cookie.setAttribute("SameSite", "None");
        } else {
            // 로컬 환경: 일반적인 Lax 설정
            cookie.setAttribute("SameSite", "Lax");
        }

        return cookie;
    }

    /**
     * 쿠키에서 사용자 ID 추출
     */
    private UUID getUserIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (USER_COOKIE_NAME.equals(cookie.getName())) {
                try {
                    return UUID.fromString(cookie.getValue());
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 UUID 형식의 쿠키: {}", cookie.getValue());
                    return null;
                }
            }
        }
        return null;
    }
}
