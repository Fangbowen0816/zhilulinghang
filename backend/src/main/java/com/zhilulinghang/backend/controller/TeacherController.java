package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.TeacherProfileRequest;
import com.zhilulinghang.backend.mapper.TeacherProfileMapper;
import com.zhilulinghang.backend.model.TeacherProfile;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TeacherController {
    private final TeacherProfileMapper teacherProfileMapper;

    public TeacherController(TeacherProfileMapper teacherProfileMapper) {
        this.teacherProfileMapper = teacherProfileMapper;
    }

    @GetMapping("/api/teachers")
    public List<TeacherProfile> listAvailableTeachers() {
        requireRole("STUDENT", "ADMIN");
        return teacherProfileMapper.findAvailableApproved();
    }

    @GetMapping("/api/teachers/{id}")
    public TeacherProfile getTeacher(@PathVariable Long id) {
        requireRole("STUDENT", "ADMIN");
        TeacherProfile profile = teacherProfileMapper.findById(id);
        if (profile == null || (!"APPROVED".equals(profile.getApprovalStatus()) || !Boolean.TRUE.equals(profile.getAvailable()))) {
            throw new IllegalArgumentException("教师资料不存在或暂不可接收请求");
        }
        return profile;
    }

    @GetMapping("/api/teacher/profile")
    public TeacherProfile getMyProfile() {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        TeacherProfile profile = teacherProfileMapper.findByTeacherId(user.getId());
        if (profile == null) {
            throw new IllegalArgumentException("教师资料不存在");
        }
        return profile;
    }

    @PutMapping("/api/teacher/profile")
    public TeacherProfile updateMyProfile(@RequestBody TeacherProfileRequest request) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        TeacherProfile existing = teacherProfileMapper.findByTeacherId(user.getId());
        if (existing == null) {
            throw new IllegalArgumentException("教师资料不存在");
        }
        if (isBlank(request.getDisplayName())) {
            throw new IllegalArgumentException("请填写展示名称");
        }

        TeacherProfile profile = new TeacherProfile();
        profile.setTeacherId(user.getId());
        profile.setDisplayName(request.getDisplayName().trim());
        profile.setDepartment(trim(request.getDepartment()));
        profile.setTitle(trim(request.getTitle()));
        profile.setBio(trim(request.getBio()));
        profile.setExpertiseTags(trim(request.getExpertiseTags()));

        String nextStatus = "REJECTED".equals(existing.getApprovalStatus()) ? "PENDING" : existing.getApprovalStatus();
        profile.setApprovalStatus(nextStatus);
        profile.setApprovalComment("PENDING".equals(nextStatus) ? existing.getApprovalComment() : null);

        boolean canBeAvailable = "APPROVED".equals(nextStatus);
        profile.setAvailable(canBeAvailable && Boolean.TRUE.equals(request.getAvailable()));
        teacherProfileMapper.updateByTeacherId(profile);
        return teacherProfileMapper.findByTeacherId(user.getId());
    }

    private AuthUser requireRole(String... allowedRoles) {
        AuthUser user = AuthContext.get();
        for (String role : allowedRoles) {
            if (role.equals(user.getRole())) {
                return user;
            }
        }
        throw new IllegalStateException("无权访问该资源");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
