package com.redflag.redflag.global.exception.code.status;

import com.redflag.redflag.global.exception.code.BaseErrorCode;
import com.redflag.redflag.global.exception.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 대시보드 관련 에러
    DASHBOARD_AGE_DATA_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "DASHBOARD4001", "연령대별 데이터를 가져올 수 없습니다."),
    DASHBOARD_AGE_LATEST_DATA_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "DASHBOARD4002", "최근 연도 연령대 데이터를 찾을 수 없습니다."),
    DASHBOARD_GENDER_DATA_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "DASHBOARD4003", "성별 데이터를 가져올 수 없습니다."),
    DASHBOARD_GENDER_LATEST_DATA_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "DASHBOARD4004", "최근 연도 성별 데이터를 찾을 수 없습니다."),
    DASHBOARD_EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DASHBOARD5001", "외부 API 조회에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
