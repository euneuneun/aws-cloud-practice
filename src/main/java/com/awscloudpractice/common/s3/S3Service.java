package com.awscloudpractice.common.s3;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

@Service
public class S3Service {

    // 속성
    private final S3Template s3Template;
    private final String bucket;

    // 생성자
    public S3Service(
            S3Template s3Template,
            @Value("${spring.cloud.aws.s3.bucket}") String bucket
    ) {
        this.s3Template = s3Template;
        this.bucket = bucket;
    }

    // 기능

    /**
     * 설정된 S3 버킷에 파일을 업로드한다.
     *
     * @param key  버킷 안에서 파일이 저장될 경로
     * @param file 업로드할 파일
     */
    public void s3Upload(String key, MultipartFile file) {

        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(file.getContentType())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucket, key, inputStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3.", e);
        }
    }

    /**
     * 버킷의 파일을 유효기간(duration) 동안 조회할 수 있는 Presigned URL을 만든다.
     *
     * @param key      조회할 파일의 S3 key (버킷 안 경로)
     * @param duration Presigned URL 유효기간
     * @return Presigned URL 문자열
     */
    public String createPresignedGetUrl(String key, Duration duration) {

        URL url = s3Template.createSignedGetURL(bucket, key, duration);

        return url.toString();
    }

    /**
     * 설정된 S3 버킷에서 파일을 영구 삭제한다.
     *
     * @param key 버킷 안에서 삭제될 파일의 경로
     */
    public void s3Delete(String key) {

        s3Template.deleteObject(bucket, key);
    }
}