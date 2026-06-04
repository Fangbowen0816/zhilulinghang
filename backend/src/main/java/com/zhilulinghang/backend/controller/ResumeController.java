package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ResumeRequest;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.model.Resume;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private final ResumeMapper resumeMapper;

    public ResumeController(ResumeMapper resumeMapper) {
        this.resumeMapper = resumeMapper;
    }

    @GetMapping("/me")
    public Resume getMyResume() {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        return resumeMapper.findLatestByStudentId(user.getId());
    }

    @PostMapping
    public Resume create(@RequestBody ResumeRequest request) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume resume = toResume(request);
        resume.setStudentId(user.getId());
        resume.setStatus("DRAFT");
        resumeMapper.insert(resume);
        return resumeMapper.findById(resume.getId());
    }

    @PutMapping("/{id}")
    public Resume update(@PathVariable Long id, @RequestBody ResumeRequest request) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume existing = requireOwnedResume(id, user);
        Resume resume = toResume(request);
        resume.setId(existing.getId());
        resumeMapper.updateContent(resume);
        return resumeMapper.findById(id);
    }

    @PostMapping("/{id}/submit")
    public Resume submit(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        requireOwnedResume(id, user);
        resumeMapper.submit(id);
        return resumeMapper.findById(id);
    }

    @GetMapping("/feedback")
    public Map<String, Object> feedback() {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume resume = resumeMapper.findLatestByStudentId(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("resume", resume);
        return result;
    }

    private Resume requireOwnedResume(Long id, AuthUser user) {
        Resume resume = resumeMapper.findById(id);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        if (!"ADMIN".equals(user.getRole()) && !resume.getStudentId().equals(user.getId())) {
            throw new IllegalStateException("不能操作他人的简历");
        }
        return resume;
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

    private Resume toResume(ResumeRequest request) {
        Resume resume = new Resume();
        resume.setName(request.getName());
        resume.setEducation(request.getEducation());
        resume.setExperience(request.getExperience());
        resume.setSkills(request.getSkills());
        return resume;
    }
}
