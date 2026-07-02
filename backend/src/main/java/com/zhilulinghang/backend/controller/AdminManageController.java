package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.PlatformSettingRequest;
import com.zhilulinghang.backend.dto.ResetPasswordRequest;
import com.zhilulinghang.backend.dto.UserEnabledRequest;
import com.zhilulinghang.backend.mapper.AdminActionLogMapper;
import com.zhilulinghang.backend.mapper.PlatformSettingMapper;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.mapper.ResumeAnnotationMapper;
import com.zhilulinghang.backend.mapper.ResumeScoreMapper;
import com.zhilulinghang.backend.mapper.ReviewRecordMapper;
import com.zhilulinghang.backend.mapper.ReviewRequestMapper;
import com.zhilulinghang.backend.mapper.TeacherProfileMapper;
import com.zhilulinghang.backend.mapper.UserMapper;
import com.zhilulinghang.backend.model.AdminActionLog;
import com.zhilulinghang.backend.model.PlatformSetting;
import com.zhilulinghang.backend.model.Resume;
import com.zhilulinghang.backend.model.ResumeAnnotation;
import com.zhilulinghang.backend.model.ResumeScore;
import com.zhilulinghang.backend.model.ReviewRecord;
import com.zhilulinghang.backend.model.ReviewRequest;
import com.zhilulinghang.backend.model.TeacherProfile;
import com.zhilulinghang.backend.model.User;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/manage")
public class AdminManageController {
    private final UserMapper userMapper;
    private final ResumeMapper resumeMapper;
    private final ResumeAnnotationMapper resumeAnnotationMapper;
    private final ResumeScoreMapper resumeScoreMapper;
    private final ReviewRequestMapper reviewRequestMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final TeacherProfileMapper teacherProfileMapper;
    private final PlatformSettingMapper platformSettingMapper;
    private final AdminActionLogMapper adminActionLogMapper;

    public AdminManageController(UserMapper userMapper, ResumeMapper resumeMapper, ResumeAnnotationMapper resumeAnnotationMapper, ResumeScoreMapper resumeScoreMapper, ReviewRequestMapper reviewRequestMapper, ReviewRecordMapper reviewRecordMapper, TeacherProfileMapper teacherProfileMapper, PlatformSettingMapper platformSettingMapper, AdminActionLogMapper adminActionLogMapper) {
        this.userMapper = userMapper;
        this.resumeMapper = resumeMapper;
        this.resumeAnnotationMapper = resumeAnnotationMapper;
        this.resumeScoreMapper = resumeScoreMapper;
        this.reviewRequestMapper = reviewRequestMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.teacherProfileMapper = teacherProfileMapper;
        this.platformSettingMapper = platformSettingMapper;
        this.adminActionLogMapper = adminActionLogMapper;
    }

    @GetMapping("/users")
    public List<User> users() {
        requireAdmin();
        return userMapper.findAll();
    }

    @PostMapping("/users/{id}/enabled")
    public User setUserEnabled(@PathVariable Long id, @RequestBody UserEnabledRequest request) {
        AuthUser admin = requireAdmin();
        if (request == null || request.getEnabled() == null) {
            throw new IllegalArgumentException("缺少用户启用状态");
        }
        User user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (admin.getId().equals(id) && Boolean.FALSE.equals(request.getEnabled())) {
            throw new IllegalStateException("不能禁用当前登录的管理员账号");
        }
        userMapper.updateEnabled(id, request.getEnabled());
        log(admin, request.getEnabled() ? "ENABLE_USER" : "DISABLE_USER", "USER", id, "username=" + user.getUsername());
        User updated = userMapper.findById(id);
        updated.setPassword(null);
        return updated;
    }

    @PostMapping("/users/{id}/password")
    public User resetUserPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        AuthUser admin = requireAdmin();
        if (request == null || isBlank(request.getNewPassword())) {
            throw new IllegalArgumentException("请输入新密码");
        }
        if (request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("新密码至少需要 6 位");
        }
        User user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.updatePassword(id, request.getNewPassword());
        log(admin, "RESET_PASSWORD", "USER", id, "username=" + user.getUsername());
        User updated = userMapper.findById(id);
        updated.setPassword(null);
        return updated;
    }

    @GetMapping("/teachers")
    public List<TeacherProfile> teachers() {
        requireAdmin();
        return teacherProfileMapper.findAll();
    }

    @PostMapping("/teachers/{id}/close")
    public TeacherProfile closeTeacherAvailable(@PathVariable Long id) {
        AuthUser admin = requireAdmin();
        TeacherProfile profile = teacherProfileMapper.findById(id);
        if (profile == null) {
            throw new IllegalArgumentException("教师资料不存在");
        }
        teacherProfileMapper.forceCloseAvailable(id);
        log(admin, "SET_TEACHER_AVAILABLE", "TEACHER_PROFILE", id, "available=false");
        return teacherProfileMapper.findById(id);
    }

    @PostMapping("/teachers/{id}/available")
    public TeacherProfile setTeacherAvailable(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        AuthUser admin = requireAdmin();
        if (payload == null || !payload.containsKey("available")) {
            throw new IllegalArgumentException("缺少接收请求状态");
        }
        TeacherProfile profile = teacherProfileMapper.findById(id);
        if (profile == null) {
            throw new IllegalArgumentException("教师资料不存在");
        }
        Boolean available = payload.get("available");
        if (Boolean.TRUE.equals(available) && !"APPROVED".equals(profile.getApprovalStatus())) {
            throw new IllegalStateException("只有审核通过的教师才能开启接收请求");
        }
        teacherProfileMapper.setAvailable(id, Boolean.TRUE.equals(available));
        log(admin, "SET_TEACHER_AVAILABLE", "TEACHER_PROFILE", id, "available=" + Boolean.TRUE.equals(available));
        return teacherProfileMapper.findById(id);
    }

    @GetMapping("/resumes")
    public List<Resume> resumes() {
        requireAdmin();
        return resumeMapper.findAll();
    }

    @GetMapping("/review-requests")
    public List<ReviewRequest> reviewRequests() {
        requireAdmin();
        return reviewRequestMapper.findAll();
    }

    @GetMapping("/review-records")
    public List<ReviewRecord> reviewRecords() {
        requireAdmin();
        return reviewRecordMapper.findAll();
    }

    @GetMapping("/resume-annotations")
    public List<ResumeAnnotation> resumeAnnotations() {
        requireAdmin();
        return resumeAnnotationMapper.findAll();
    }

    @GetMapping("/resume-scores")
    public List<ResumeScore> resumeScores() {
        requireAdmin();
        return resumeScoreMapper.findAll();
    }

    @GetMapping("/settings")
    public List<PlatformSetting> settings() {
        requireAdmin();
        return platformSettingMapper.findAll();
    }

    @PostMapping("/settings")
    public PlatformSetting createSetting(@RequestBody PlatformSettingRequest request) {
        AuthUser admin = requireAdmin();
        if (request == null || isBlank(request.getSettingKey())) {
            throw new IllegalArgumentException("请输入设置键名");
        }
        String key = request.getSettingKey().trim();
        if (!key.matches("[A-Za-z0-9_.-]{2,80}")) {
            throw new IllegalArgumentException("设置键名只能包含字母、数字、下划线、点和短横线，长度 2-80");
        }
        if (platformSettingMapper.findByKey(key) != null) {
            throw new IllegalArgumentException("设置键名已存在");
        }
        PlatformSetting setting = new PlatformSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(trim(request.getSettingValue()));
        setting.setDescription(trim(request.getDescription()));
        platformSettingMapper.insert(setting);
        log(admin, "CREATE_SETTING", "PLATFORM_SETTING", setting.getId(), "key=" + key);
        return platformSettingMapper.findById(setting.getId());
    }

    @PostMapping("/settings/{id}")
    public PlatformSetting updateSetting(@PathVariable Long id, @RequestBody PlatformSettingRequest request) {
        AuthUser admin = requireAdmin();
        PlatformSetting setting = platformSettingMapper.findById(id);
        if (setting == null) {
            throw new IllegalArgumentException("平台设置不存在");
        }
        platformSettingMapper.update(id, request == null ? null : trim(request.getSettingValue()), request == null ? null : trim(request.getDescription()));
        log(admin, "UPDATE_SETTING", "PLATFORM_SETTING", id, "key=" + setting.getSettingKey());
        return platformSettingMapper.findById(id);
    }

    @GetMapping("/action-logs")
    public List<AdminActionLog> actionLogs() {
        requireAdmin();
        return adminActionLogMapper.findRecent();
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
