package com.zhilulinghang.backend.dto;

import java.util.List;

public class ReviewRequestCreateRequest {
    private Long sourceResumeId;
    private String assignMode;
    private List<Long> teacherIds;
    private String studentMessage;

    public Long getSourceResumeId() {
        return sourceResumeId;
    }

    public void setSourceResumeId(Long sourceResumeId) {
        this.sourceResumeId = sourceResumeId;
    }

    public String getAssignMode() {
        return assignMode;
    }

    public void setAssignMode(String assignMode) {
        this.assignMode = assignMode;
    }

    public List<Long> getTeacherIds() {
        return teacherIds;
    }

    public void setTeacherIds(List<Long> teacherIds) {
        this.teacherIds = teacherIds;
    }

    public String getStudentMessage() {
        return studentMessage;
    }

    public void setStudentMessage(String studentMessage) {
        this.studentMessage = studentMessage;
    }
}
