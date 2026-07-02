package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.TeacherProfileRequest;
import com.zhilulinghang.backend.mapper.AdminActionLogMapper;
import com.zhilulinghang.backend.mapper.TeacherProfileMapper;
import com.zhilulinghang.backend.model.AdminActionLog;
import com.zhilulinghang.backend.model.TeacherProfile;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import com.zhilulinghang.backend.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teacher-profiles")
public class AdminTeacherProfileController {
    private final TeacherProfileMapper teacherProfileMapper;
    private final AdminActionLogMapper adminActionLogMapper;
    private final NotificationService notificationService;

    public AdminTeacherProfileController(TeacherProfileMapper teacherProfileMapper, AdminActionLogMapper adminActionLogMapper, NotificationService notificationService) {
        this.teacherProfileMapper = teacherProfileMapper;
        this.adminActionLogMapper = adminActionLogMapper;
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<TeacherProfile> listAll() {
        requireAdmin();
        return teacherProfileMapper.findAll();
    }

    @GetMapping("/pending")
    public List<TeacherProfile> listPending() {
        requireAdmin();
        return teacherProfileMapper.findPending();
    }

    @PostMapping("/{id}/approve")
    public TeacherProfile approve(@PathVariable Long id) {
        AuthUser admin = requireAdmin();
        requireProfile(id);
        teacherProfileMapper.approve(id, admin.getId());
        log(admin, "APPROVE_TEACHER_PROFILE", "TEACHER_PROFILE", id, null);
        TeacherProfile updated = teacherProfileMapper.findById(id);
        notificationService.notifyUser(updated.getTeacherId(), "TEACHER_PROFILE_APPROVED", "教师资料审核通过", "你的教师资料已通过审核。", "TEACHER_PROFILE", id);
        return updated;
    }

    @PostMapping("/{id}/reject")
    public TeacherProfile reject(@PathVariable Long id, @RequestBody TeacherProfileRequest request) {
        AuthUser admin = requireAdmin();
        requireProfile(id);
        if (request == null || request.getApprovalComment() == null || request.getApprovalComment().trim().isEmpty()) {
            throw new IllegalArgumentException("拒绝教师资料时必须填写原因");
        }
        teacherProfileMapper.reject(id, admin.getId(), request.getApprovalComment().trim());
        log(admin, "REJECT_TEACHER_PROFILE", "TEACHER_PROFILE", id, "reason=" + request.getApprovalComment().trim());
        TeacherProfile updated = teacherProfileMapper.findById(id);
        notificationService.notifyUser(updated.getTeacherId(), "TEACHER_PROFILE_REJECTED", "教师资料审核未通过", request.getApprovalComment().trim(), "TEACHER_PROFILE", id);
        return updated;
    }

    private TeacherProfile requireProfile(Long id) {
        TeacherProfile profile = teacherProfileMapper.findById(id);
        if (profile == null) {
            throw new IllegalArgumentException("教师资料不存在");
        }
        return profile;
    }

    private AuthUser requireAdmin() {
        AuthUser user = AuthContext.get();
        if (!"ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("无权访问该资源");
        }
        return user;
    }

    private void log(AuthUser admin, String action, String targetType, Long targetId, String detail) {
        AdminActionLog log = new AdminActionLog();
        log.setAdminId(admin.getId());
        log.setAdminUsername(admin.getUsername());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        adminActionLogMapper.insert(log);
    }
}
