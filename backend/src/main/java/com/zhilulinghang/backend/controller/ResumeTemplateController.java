package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.mapper.ResumeTemplateMapper;
import com.zhilulinghang.backend.model.ResumeTemplate;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resume-templates")
public class ResumeTemplateController {
    private final ResumeTemplateMapper resumeTemplateMapper;

    public ResumeTemplateController(ResumeTemplateMapper resumeTemplateMapper) {
        this.resumeTemplateMapper = resumeTemplateMapper;
    }

    @GetMapping
    public List<ResumeTemplate> list() {
        requireRole("STUDENT", "ADMIN");
        return resumeTemplateMapper.findEnabled();
    }

    @GetMapping("/{id}")
    public ResumeTemplate detail(@PathVariable Long id) {
        requireRole("STUDENT", "ADMIN");
        ResumeTemplate template = resumeTemplateMapper.findById(id);
        if (template == null || !Boolean.TRUE.equals(template.getEnabled())) {
            throw new IllegalArgumentException("简历模板不存在或已停用");
        }
        return template;
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
