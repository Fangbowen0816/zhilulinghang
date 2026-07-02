package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ResumeAnnotationRequest;
import com.zhilulinghang.backend.dto.ResumeScoreRequest;
import com.zhilulinghang.backend.mapper.ResumeAnnotationMapper;
import com.zhilulinghang.backend.mapper.ResumeScoreMapper;
import com.zhilulinghang.backend.mapper.ReviewRequestMapper;
import com.zhilulinghang.backend.model.ResumeAnnotation;
import com.zhilulinghang.backend.model.ResumeScore;
import com.zhilulinghang.backend.model.ReviewRequest;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ResumeFeedbackController {
    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "title",
            "name",
            "phone",
            "email",
            "target_position",
            "education",
            "experience",
            "skills",
            "awards",
            "self_evaluation"
    );
    private static final Set<String> ALLOWED_MARK_TYPES = Set.of("TEXT", "STRUCTURE", "KEYWORD");

    private final ReviewRequestMapper reviewRequestMapper;
    private final ResumeAnnotationMapper resumeAnnotationMapper;
    private final ResumeScoreMapper resumeScoreMapper;

    public ResumeFeedbackController(ReviewRequestMapper reviewRequestMapper, ResumeAnnotationMapper resumeAnnotationMapper, ResumeScoreMapper resumeScoreMapper) {
        this.reviewRequestMapper = reviewRequestMapper;
        this.resumeAnnotationMapper = resumeAnnotationMapper;
        this.resumeScoreMapper = resumeScoreMapper;
    }

    @GetMapping("/review-requests/{id}/annotations")
    public List<ResumeAnnotation> annotations(@PathVariable Long id) {
        AuthUser user = AuthContext.get();
        requireVisibleRequest(id, user);
        return resumeAnnotationMapper.findByRequestId(id);
    }

    @PostMapping("/review-requests/{id}/annotations")
    public ResumeAnnotation createAnnotation(@PathVariable Long id, @RequestBody ResumeAnnotationRequest body) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        ReviewRequest request = requireFeedbackWritableRequest(id, user);
        if (body == null || !StringUtils.hasText(body.getFieldName())) {
            throw new IllegalArgumentException("fieldName is required");
        }
        if (!StringUtils.hasText(body.getContent())) {
            throw new IllegalArgumentException("content is required");
        }
        String fieldName = body.getFieldName().trim();
        if (!ALLOWED_FIELDS.contains(fieldName)) {
            throw new IllegalArgumentException("unsupported resume field");
        }
        String markType = StringUtils.hasText(body.getMarkType()) ? body.getMarkType().trim().toUpperCase(Locale.ROOT) : "TEXT";
        if (!ALLOWED_MARK_TYPES.contains(markType)) {
            throw new IllegalArgumentException("unsupported mark type");
        }

        ResumeAnnotation annotation = new ResumeAnnotation();
        annotation.setRequestId(request.getId());
        annotation.setResumeId(request.getSourceResumeId());
        annotation.setTeacherId(request.getTeacherId());
        annotation.setStudentId(request.getStudentId());
        annotation.setFieldName(fieldName);
        annotation.setMarkType(markType);
        annotation.setContent(body.getContent().trim());
        resumeAnnotationMapper.insert(annotation);
        return resumeAnnotationMapper.findById(annotation.getId());
    }

    @DeleteMapping("/resume-annotations/{id}")
    public void deleteAnnotation(@PathVariable Long id) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        ResumeAnnotation annotation = resumeAnnotationMapper.findById(id);
        if (annotation == null) {
            throw new IllegalArgumentException("annotation not found");
        }
        if (!"ADMIN".equals(user.getRole()) && !annotation.getTeacherId().equals(user.getId())) {
            throw new IllegalStateException("cannot delete another teacher's annotation");
        }
        resumeAnnotationMapper.deleteById(id);
    }

    @GetMapping("/review-requests/{id}/score")
    public ResumeScore score(@PathVariable Long id) {
        AuthUser user = AuthContext.get();
        requireVisibleRequest(id, user);
        return resumeScoreMapper.findByRequestId(id);
    }

    @PostMapping("/review-requests/{id}/score")
    public ResumeScore saveScore(@PathVariable Long id, @RequestBody ResumeScoreRequest body) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        ReviewRequest request = requireFeedbackWritableRequest(id, user);
        if (body == null || body.getScore() == null) {
            throw new IllegalArgumentException("score is required");
        }
        if (body.getScore() < 0 || body.getScore() > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }

        ResumeScore score = new ResumeScore();
        score.setRequestId(request.getId());
        score.setResumeId(request.getSourceResumeId());
        score.setTeacherId(request.getTeacherId());
        score.setStudentId(request.getStudentId());
        score.setScore(body.getScore());
        score.setRemark(trim(body.getRemark()));

        if (resumeScoreMapper.findByRequestId(id) == null) {
            resumeScoreMapper.insert(score);
        } else {
            resumeScoreMapper.updateByRequestId(score);
        }
        return resumeScoreMapper.findByRequestId(id);
    }

    private ReviewRequest requireFeedbackWritableRequest(Long id, AuthUser user) {
        ReviewRequest request = requireRequest(id);
        if (!"ADMIN".equals(user.getRole()) && !request.getTeacherId().equals(user.getId())) {
            throw new IllegalStateException("cannot edit another teacher's review request");
        }
        if (!"ACCEPTED".equals(request.getStatus())) {
            throw new IllegalArgumentException("only accepted review requests can be annotated or scored");
        }
        return request;
    }

    private ReviewRequest requireVisibleRequest(Long id, AuthUser user) {
        ReviewRequest request = requireRequest(id);
        if ("ADMIN".equals(user.getRole())) {
            return request;
        }
        if ("STUDENT".equals(user.getRole()) && request.getStudentId().equals(user.getId())) {
            return request;
        }
        if ("TEACHER".equals(user.getRole()) && request.getTeacherId().equals(user.getId())) {
            return request;
        }
        throw new IllegalStateException("access denied");
    }

    private ReviewRequest requireRequest(Long id) {
        ReviewRequest request = reviewRequestMapper.findById(id);
        if (request == null) {
            throw new IllegalArgumentException("review request not found");
        }
        return request;
    }

    private AuthUser requireRole(String... allowedRoles) {
        AuthUser user = AuthContext.get();
        for (String role : allowedRoles) {
            if (role.equals(user.getRole())) {
                return user;
            }
        }
        throw new IllegalStateException("access denied");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
