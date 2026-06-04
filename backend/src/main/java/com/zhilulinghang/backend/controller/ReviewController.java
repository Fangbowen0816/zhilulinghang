package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ReviewRequest;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.model.Resume;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private final ResumeMapper resumeMapper;

    public ReviewController(ResumeMapper resumeMapper) {
        this.resumeMapper = resumeMapper;
    }

    @GetMapping("/list")
    public List<Resume> list() {
        requireTeacherOrAdmin();
        return resumeMapper.findSubmitted();
    }

    @GetMapping("/{id}")
    public Resume detail(@PathVariable Long id) {
        requireTeacherOrAdmin();
        Resume resume = resumeMapper.findById(id);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        return resume;
    }

    @PostMapping("/{id}")
    public Resume review(@PathVariable Long id, @RequestBody ReviewRequest request) {
        requireTeacherOrAdmin();
        Resume resume = resumeMapper.findById(id);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        String status = request.getStatus();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("审核状态只能是 APPROVED 或 REJECTED");
        }
        resumeMapper.review(id, status, request.getTeacherComment());
        return resumeMapper.findById(id);
    }

    private void requireTeacherOrAdmin() {
        AuthUser user = AuthContext.get();
        if (!"TEACHER".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("无权访问教师审核资源");
        }
    }
}
