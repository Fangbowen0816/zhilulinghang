package com.zhilulinghang.backend.service;

import com.zhilulinghang.backend.mapper.NotificationMapper;
import com.zhilulinghang.backend.mapper.UserMapper;
import com.zhilulinghang.backend.model.Notification;
import com.zhilulinghang.backend.model.User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public NotificationService(NotificationMapper notificationMapper, UserMapper userMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
    }

    public void notifyUser(Long userId, String type, String title, String content, String relatedType, Long relatedId) {
        if (userId == null || !StringUtils.hasText(title)) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(defaultText(type, "GENERAL"));
        notification.setTitle(title.trim());
        notification.setContent(trim(content));
        notification.setRelatedType(trim(relatedType));
        notification.setRelatedId(relatedId);
        notificationMapper.insert(notification);
    }

    public void notifyAdmins(String type, String title, String content, String relatedType, Long relatedId) {
        List<User> admins = userMapper.findByRole("ADMIN");
        for (User admin : admins) {
            notifyUser(admin.getId(), type, title, content, relatedType, relatedId);
        }
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
