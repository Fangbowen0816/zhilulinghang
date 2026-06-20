package com.zhilulinghang.backend.model;

import java.time.LocalDateTime;

public class ReviewRecord {
    private Long id;
    private Long requestId;
    private Long sourceResumeId;
    private Long returnedResumeId;
    private Long studentId;
    private Long teacherId;
    private String action;
    private String comment;
    private Boolean teacherDeleted;
    private LocalDateTime createTime;
    private String sourceResumeTitle;
    private String returnedResumeTitle;
    private String teacherDisplayName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getSourceResumeId() {
        return sourceResumeId;
    }

    public void setSourceResumeId(Long sourceResumeId) {
        this.sourceResumeId = sourceResumeId;
    }

    public Long getReturnedResumeId() {
        return returnedResumeId;
    }

    public void setReturnedResumeId(Long returnedResumeId) {
        this.returnedResumeId = returnedResumeId;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getTeacherDeleted() {
        return teacherDeleted;
    }

    public void setTeacherDeleted(Boolean teacherDeleted) {
        this.teacherDeleted = teacherDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getSourceResumeTitle() {
        return sourceResumeTitle;
    }

    public void setSourceResumeTitle(String sourceResumeTitle) {
        this.sourceResumeTitle = sourceResumeTitle;
    }

    public String getReturnedResumeTitle() {
        return returnedResumeTitle;
    }

    public void setReturnedResumeTitle(String returnedResumeTitle) {
        this.returnedResumeTitle = returnedResumeTitle;
    }

    public String getTeacherDisplayName() {
        return teacherDisplayName;
    }

    public void setTeacherDisplayName(String teacherDisplayName) {
        this.teacherDisplayName = teacherDisplayName;
    }
}
