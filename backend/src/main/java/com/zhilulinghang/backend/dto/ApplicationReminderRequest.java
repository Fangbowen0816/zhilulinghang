package com.zhilulinghang.backend.dto;

import java.time.LocalDateTime;

public class ApplicationReminderRequest {
    private String remindType;
    private LocalDateTime remindTime;
    private String content;

    public String getRemindType() {
        return remindType;
    }

    public void setRemindType(String remindType) {
        this.remindType = remindType;
    }

    public LocalDateTime getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(LocalDateTime remindTime) {
        this.remindTime = remindTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
