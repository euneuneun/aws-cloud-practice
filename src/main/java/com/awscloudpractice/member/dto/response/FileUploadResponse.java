package com.awscloudpractice.member.dto.response;

public class FileUploadResponse {

    // 속성
    private final String key;

    // 생성자
    public FileUploadResponse(String key) {
        this.key = key;
    }

    // 기능
    public String getKey() {
        return key;
    }
}