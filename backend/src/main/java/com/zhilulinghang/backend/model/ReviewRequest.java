package com.zhilulinghang.backend.model;

import java.time.LocalDateTime;

public class ReviewRequest {
    private Long id;
    private Long sourceResumeId;
    private Long studentId;
    private Long teacherId;
    private String assignMode;
    private String status;
    private String studentMessage;
    private String teacherReply;
    private String declineReason;
    private String declineSuggestion;
    private String withdrawReason;
    private String adminDecision;
    private String adminComment;
    private Long adminId;
    private LocalDateTime adminTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String sourceResumeTitle;
    private String teacherDisplayName;
    private String studentUsername;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceResumeId() {
        return sourceResumeId;
    }

    public void setSourceResumeId(Long sourceResumeId) {
        this.sourceResumeId = sourceResumeId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getAssignMode() {
        return assignMode;
    }

    public void setAssignMode(String assignMode) {
        this.assignMode = assignMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentMessage() {
        return studentMessage;
    }

    public void setStudentMessage(String studentMessage) {
        this.studentMessage = studentMessage;
    }

    public String getTeacherReply() {
        return teacherReply;
    }

    public void setTeacherReply(String teacherReply) {
        this.teacherReply = teacherReply;
    }

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

    public String getWithdrawReason() {
        return withdrawReason;
    }

    public void setWithdrawReason(String withdrawReason) {
        this.withdrawReason = withdrawReason;
    }

    public String getAdminDecision() {
        return adminDecision;
    }

    public void setAdminDecision(String adminDecision) {
        this.adminDecision = adminDecision;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public LocalDateTime getAdminTime() {
        return adminTime;
    }

    public void setAdminTime(LocalDateTime adminTime) {
        this.adminTime = adminTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getSourceResumeTitle() {
        return sourceResumeTitle;
    }

    public void setSourceResumeTitle(String sourceResumeTitle) {
        this.sourceResumeTitle = sourceResumeTitle;
    }

    public String getTeacherDisplayName() {
        return teacherDisplayName;
    }

    public void setTeacherDisplayName(String teacherDisplayName) {
        this.teacherDisplayName = teacherDisplayName;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }
}
