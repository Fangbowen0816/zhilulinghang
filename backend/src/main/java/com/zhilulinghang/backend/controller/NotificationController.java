package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.mapper.NotificationMapper;
import com.zhilulinghang.backend.model.Notification;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public List<Notification> list() {
        AuthUser user = AuthContext.get();
        return notificationMapper.findByUserId(user.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Integer> unreadCount() {
        AuthUser user = AuthContext.get();
        return Map.of("count", notificationMapper.countUnread(user.getId()));
    }

    @PostMapping("/{id}/read")
    public Map<String, Boolean> markRead(@PathVariable Long id) {
        AuthUser user = AuthContext.get();
        notificationMapper.markRead(id, user.getId());
        return Map.of("success", true);
    }

    @PostMapping("/read-all")
    public Map<String, Boolean> markAllRead() {
        AuthUser user = AuthContext.get();
        notificationMapper.markAllRead(user.getId());
        return Map.of("success", true);
    }
}
