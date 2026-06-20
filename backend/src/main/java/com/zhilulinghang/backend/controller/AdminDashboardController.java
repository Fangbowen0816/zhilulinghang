package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.mapper.ReviewRecordMapper;
import com.zhilulinghang.backend.mapper.ReviewRequestMapper;
import com.zhilulinghang.backend.mapper.TeacherProfileMapper;
import com.zhilulinghang.backend.mapper.UserMapper;
import com.zhilulinghang.backend.security.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final UserMapper userMapper;
    private final ResumeMapper resumeMapper;
    private final ReviewRequestMapper reviewRequestMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final TeacherProfileMapper teacherProfileMapper;

    public AdminDashboardController(UserMapper userMapper, ResumeMapper resumeMapper, ReviewRequestMapper reviewRequestMapper, ReviewRecordMapper reviewRecordMapper, TeacherProfileMapper teacherProfileMapper) {
        this.userMapper = userMapper;
        this.resumeMapper = resumeMapper;
        this.reviewRequestMapper = reviewRequestMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.teacherProfileMapper = teacherProfileMapper;
    }

    @GetMapping
    public Map<String, Object> dashboard() {
        requireAdmin();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentCount", userMapper.countByRole("STUDENT"));
        result.put("teacherCount", userMapper.countByRole("TEACHER"));
        result.put("pendingTeacherCount", teacherProfileMapper.countByApprovalStatus("PENDING"));
        result.put("resumeCount", resumeMapper.countAll());
        result.put("frozenResumeCount", resumeMapper.countFrozen());
        result.put("reviewRequestCount", reviewRequestMapper.countAll());
        result.put("pendingRequestCount", reviewRequestMapper.countByStatus("PENDING"));
        result.put("acceptedRequestCount", reviewRequestMapper.countByStatus("ACCEPTED"));
        result.put("withdrawPendingCount", reviewRequestMapper.countByStatus("WITHDRAW_PENDING"));
        result.put("completedRequestCount", reviewRequestMapper.countByStatus("COMPLETED"));
        result.put("reviewRecordCount", reviewRecordMapper.countAll());
        return result;
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(AuthContext.get().getRole())) {
            throw new IllegalStateException("无权访问该资源");
        }
    }
}
