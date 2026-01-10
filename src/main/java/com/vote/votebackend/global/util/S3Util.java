package com.vote.votebackend.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Util {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * Presigned URL 발급 (프론트엔드가 이미지를 직접 업로드할 수 있는 5분짜리 티켓)
     *
     * @param originalFilename 원본 파일명 (예: myface.jpg)
     * @param folder           폴더명 (예: profile)
     * @return [0]: Presigned URL (업로드용), [1]: 실제 저장된 파일 경로 (DB 저장용)
     */

    public String[] getPresignedUrl(String originalFilename, String folder) {

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String contentType = getContentType(ext);
        String filename = folder + "/" + UUID.randomUUID() + "." + ext;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String url = presignedRequest.url().toString();

        log.info("presigned url : {}", url);

        return new String[]{url, filename};
    }

    private String getContentType(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }


}
