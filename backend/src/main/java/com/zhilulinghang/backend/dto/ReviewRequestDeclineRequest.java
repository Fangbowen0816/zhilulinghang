package com.zhilulinghang.backend.dto;

public class ReviewRequestDeclineRequest {
    private String declineReason;
    private String declineSuggestion;

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public String getDeclineSuggestion() {
        return declineSuggestion;
    }

    public void setDeclineSuggestion(String declineSuggestion) {
        this.declineSuggestion = declineSuggestion;
    }
}
