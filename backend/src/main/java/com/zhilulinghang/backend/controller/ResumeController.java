package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ResumeRequest;
import com.zhilulinghang.backend.dto.ResumePolishRequest;
import com.zhilulinghang.backend.dto.ResumePolishResponse;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.model.Resume;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import com.zhilulinghang.backend.service.ResumePolishService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private final ResumeMapper resumeMapper;
    private final ResumePolishService resumePolishService;

    public ResumeController(ResumeMapper resumeMapper, ResumePolishService resumePolishService) {
        this.resumeMapper = resumeMapper;
        this.resumePolishService = resumePolishService;
    }

    @GetMapping("/me")
    public Resume getMyResume() {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        return resumeMapper.findLatestByStudentId(user.getId());
    }

    @GetMapping("/my")
    public List<Resume> getMyResumes() {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        return resumeMapper.findByStudentId(user.getId());
    }

    @GetMapping("/{id}")
    public Resume getById(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        return requireOwnedResume(id, user);
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

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        requireOwnedResume(id, user);
        resumeMapper.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("deleted", true);
        result.put("id", id);
        return result;
    }

    @GetMapping("/feedback")
    public Map<String, Object> feedback() {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        List<Resume> resumes = resumeMapper.findByStudentId(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("resumes", resumes);
        result.put("resume", resumes.isEmpty() ? null : resumes.get(0));
        return result;
    }

    @PostMapping("/polish")
    public ResumePolishResponse polish(@RequestBody ResumePolishRequest request) {
        requireRole("STUDENT", "ADMIN");
        return resumePolishService.polish(request);
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
        resume.setTitle(defaultTitle(request));
        resume.setName(request.getName());
        resume.setPhone(request.getPhone());
        resume.setEmail(request.getEmail());
        resume.setTargetPosition(request.getTargetPosition());
        resume.setEducation(request.getEducation());
        resume.setExperience(request.getExperience());
        resume.setSkills(request.getSkills());
        resume.setAwards(request.getAwards());
        resume.setSelfEvaluation(request.getSelfEvaluation());
        return resume;
    }

    private String defaultTitle(ResumeRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            return request.getTitle().trim();
        }
        if (StringUtils.hasText(request.getName()) && StringUtils.hasText(request.getTargetPosition())) {
            return request.getName().trim() + " - " + request.getTargetPosition().trim();
        }
        if (StringUtils.hasText(request.getTargetPosition())) {
            return request.getTargetPosition().trim() + "简历";
        }
        return "我的简历";
    }
}
