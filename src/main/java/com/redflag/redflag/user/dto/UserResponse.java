package com.redflag.redflag.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String message
) {
    public static UserResponse of(UUID userId) {
        return new UserResponse(userId, "사용자 ID가 생성되었습니다.");
    }
}
