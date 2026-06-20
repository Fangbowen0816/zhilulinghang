package com.zhilulinghang.backend.dto;

public class ReviewReturnRequest extends ResumeRequest {
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
