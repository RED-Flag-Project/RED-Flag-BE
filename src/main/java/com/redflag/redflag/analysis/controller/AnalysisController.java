package com.redflag.redflag.analysis.controller;

import com.redflag.redflag.analysis.dto.AnalysisUploadResponse;
import com.redflag.redflag.analysis.service.AnalysisService;
import com.redflag.redflag.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "이미지 분석 API")
public class AnalysisController {
    
    private final AnalysisService analysisService;
    
    @Operation(
        summary = "이미지 업로드 및 분석 요청",
        description = "사용자가 채팅 이미지를 업로드하고 분석을 시작합니다. " +
                     "S3 업로드, ML 분석, DB 저장을 모두 처리합니다."
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AnalysisUploadResponse> uploadAndAnalyze(
            @Parameter(description = "사용자 UUID", required = true)
            @RequestHeader("User-UUID") String userUuid,
            
            @Parameter(description = "분석할 이미지 파일 (JPG, PNG)", required = true)
            @RequestPart("image") MultipartFile image
    ) {
        log.info("이미지 업로드 요청 - 사용자: {}, 파일명: {}", userUuid, image.getOriginalFilename());
        
        AnalysisUploadResponse response = analysisService.uploadAndAnalyze(userUuid, image);
        return ApiResponse.onSuccess(response);
    }
}
