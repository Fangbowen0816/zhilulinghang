package com.zhilulinghang.backend.dto;

public class ReviewRequestWithdrawRequest {
    private String reason;
    private String adminComment;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}
