package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.mapper.ReviewRecordMapper;
import com.zhilulinghang.backend.model.ReviewRecord;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/review-records")
public class ReviewRecordController {
    private final ReviewRecordMapper reviewRecordMapper;

    public ReviewRecordController(ReviewRecordMapper reviewRecordMapper) {
        this.reviewRecordMapper = reviewRecordMapper;
    }

    @GetMapping("/student")
    public List<ReviewRecord> listStudentRecords() {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        return reviewRecordMapper.findByStudentId(user.getId());
    }

    @GetMapping("/teacher")
    public List<ReviewRecord> listTeacherRecords() {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        return reviewRecordMapper.findVisibleByTeacherId(user.getId());
    }

    @PostMapping("/{id}/hide")
    public ReviewRecord hideForTeacher(@PathVariable Long id) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        ReviewRecord record = reviewRecordMapper.findById(id);
        if (record == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        if (!"ADMIN".equals(user.getRole()) && !record.getTeacherId().equals(user.getId())) {
            throw new IllegalStateException("不能隐藏他人的审核记录");
        }
        reviewRecordMapper.hideForTeacher(id, record.getTeacherId());
        return reviewRecordMapper.findById(id);
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
}
