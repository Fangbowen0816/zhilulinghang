package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ResumeRequest;
import com.zhilulinghang.backend.dto.ResumePolishRequest;
import com.zhilulinghang.backend.dto.ResumePolishResponse;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.mapper.ResumeTemplateMapper;
import com.zhilulinghang.backend.model.Resume;
import com.zhilulinghang.backend.model.ResumeTemplate;
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
    private final ResumeTemplateMapper resumeTemplateMapper;
    private final ResumePolishService resumePolishService;

    public ResumeController(ResumeMapper resumeMapper, ResumeTemplateMapper resumeTemplateMapper, ResumePolishService resumePolishService) {
        this.resumeMapper = resumeMapper;
        this.resumeTemplateMapper = resumeTemplateMapper;
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

    @GetMapping("/{id}/versions")
    public List<Resume> getVersions(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume source = requireOwnedResume(id, user);
        return resumeMapper.findVersionsBySourceResumeId(source.getId());
    }

    @GetMapping("/{id}/preview")
    public Map<String, Object> preview(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume resume = requireOwnedResume(id, user);
        ResumeTemplate template = resolveTemplate(resume.getTemplateId());
        Map<String, Object> result = new HashMap<>();
        result.put("html", buildResumeHtml(resume, template));
        result.put("template", template);
        return result;
    }

    @GetMapping("/{id}/export")
    public Map<String, Object> export(@PathVariable Long id) {
        return preview(id);
    }

    @PostMapping
    public Resume create(@RequestBody ResumeRequest request) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume resume = toResume(request);
        resume.setStudentId(user.getId());
        resume.setStatus("DRAFT");
        resume.setFrozen(false);
        resumeMapper.insert(resume);
        return resumeMapper.findById(resume.getId());
    }

    @PutMapping("/{id}")
    public Resume update(@PathVariable Long id, @RequestBody ResumeRequest request) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume existing = requireOwnedResume(id, user);
        requireNotFrozen(existing);
        Resume resume = toResume(request);
        resume.setId(existing.getId());
        resumeMapper.updateContent(resume);
        return resumeMapper.findById(id);
    }

    @PostMapping("/{id}/submit")
    public Resume submit(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume resume = requireOwnedResume(id, user);
        requireNotFrozen(resume);
        resumeMapper.submit(id);
        return resumeMapper.findById(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume resume = requireOwnedResume(id, user);
        requireNotFrozen(resume);
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
        resume.setVersionType("ORIGINAL");
        resume.setTemplateId(resolveTemplateId(request.getTemplateId()));
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

    private Long resolveTemplateId(Long templateId) {
        if (templateId == null) {
            ResumeTemplate template = resumeTemplateMapper.findDefault();
            return template == null ? null : template.getId();
        }
        ResumeTemplate template = resumeTemplateMapper.findById(templateId);
        if (template == null || !Boolean.TRUE.equals(template.getEnabled())) {
            throw new IllegalArgumentException("简历模板不存在或已停用");
        }
        return templateId;
    }

    private ResumeTemplate resolveTemplate(Long templateId) {
        ResumeTemplate template = templateId == null ? null : resumeTemplateMapper.findById(templateId);
        if (template == null || !Boolean.TRUE.equals(template.getEnabled())) {
            template = resumeTemplateMapper.findDefault();
        }
        return template;
    }

    private String buildResumeHtml(Resume resume, ResumeTemplate template) {
        String style = template == null ? "CLASSIC" : template.getStyle();
        String accent = "COMPACT".equals(style) ? "#155e75" : "MODERN".equals(style) ? "#2563eb" : "#1f2937";
        String maxWidth = "COMPACT".equals(style) ? "760px" : "820px";
        String sectionOrder = template == null || template.getStructure() == null ? "basic,education,experience,skills,awards,selfEvaluation" : template.getStructure();
        StringBuilder sections = new StringBuilder();
        for (String key : sectionOrder.split(",")) {
            appendSection(sections, key.trim(), resume);
        }
        return """
                <!doctype html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <title>%s</title>
                  <style>
                    * { box-sizing: border-box; }
                    body { margin: 0; padding: 32px; background: #f3f4f6; color: #111827; font-family: Arial, "Microsoft YaHei", sans-serif; }
                    .resume { max-width: %s; margin: 0 auto; padding: 42px; background: #fff; border: 1px solid #e5e7eb; }
                    .header { border-bottom: 3px solid %s; padding-bottom: 18px; margin-bottom: 24px; }
                    h1 { margin: 0 0 8px; font-size: 30px; letter-spacing: 0; }
                    .target { margin: 0 0 10px; color: %s; font-size: 17px; font-weight: 700; }
                    .contact { display: flex; gap: 16px; flex-wrap: wrap; color: #4b5563; font-size: 14px; }
                    section { margin: 22px 0; }
                    h2 { margin: 0 0 10px; color: %s; font-size: 17px; border-left: 4px solid %s; padding-left: 10px; letter-spacing: 0; }
                    p { margin: 0; white-space: pre-wrap; line-height: 1.75; }
                    @media print { body { padding: 0; background: #fff; } .resume { border: 0; max-width: none; } }
                  </style>
                </head>
                <body>
                  <article class="resume">
                    <header class="header">
                      <h1>%s</h1>
                      <p class="target">%s</p>
                      <div class="contact">%s%s</div>
                    </header>
                    %s
                  </article>
                </body>
                </html>
                """.formatted(
                escape(defaultText(resume.getTitle(), "简历预览")),
                maxWidth,
                accent,
                accent,
                accent,
                accent,
                escape(defaultText(resume.getName(), "姓名")),
                escape(defaultText(resume.getTargetPosition(), "求职意向未填写")),
                resume.getPhone() == null || resume.getPhone().isBlank() ? "" : "<span>" + escape(resume.getPhone()) + "</span>",
                resume.getEmail() == null || resume.getEmail().isBlank() ? "" : "<span>" + escape(resume.getEmail()) + "</span>",
                sections
        );
    }

    private void appendSection(StringBuilder builder, String key, Resume resume) {
        if ("education".equals(key)) appendSection(builder, "教育经历", resume.getEducation());
        if ("experience".equals(key)) appendSection(builder, "项目经历", resume.getExperience());
        if ("skills".equals(key)) appendSection(builder, "技能", resume.getSkills());
        if ("awards".equals(key)) appendSection(builder, "奖项证书", resume.getAwards());
        if ("selfEvaluation".equals(key)) appendSection(builder, "自我评价", resume.getSelfEvaluation());
    }

    private void appendSection(StringBuilder builder, String title, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        builder.append("<section><h2>")
                .append(escape(title))
                .append("</h2><p>")
                .append(escape(content))
                .append("</p></section>");
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void requireNotFrozen(Resume resume) {
        if (Boolean.TRUE.equals(resume.getFrozen())) {
            throw new IllegalStateException("该简历正在撤回处理中，暂时不能操作");
        }
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
