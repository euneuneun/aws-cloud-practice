package com.awscloudpractice.member.dto.response;

public class FileDownloadUrlResponse {

    // 속성
    private final String url;

    // 생성자
    public FileDownloadUrlResponse(String url) {
        this.url = url;
    }

    // 기능
    public String getUrl() {
        return url;
    }
}
