package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ReviewRequestCreateRequest;
import com.zhilulinghang.backend.dto.ReviewRequestDeclineRequest;
import com.zhilulinghang.backend.dto.ReviewRequestWithdrawRequest;
import com.zhilulinghang.backend.dto.ReviewReturnRequest;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.mapper.ReviewRecordMapper;
import com.zhilulinghang.backend.mapper.ReviewRequestMapper;
import com.zhilulinghang.backend.mapper.TeacherProfileMapper;
import com.zhilulinghang.backend.model.Resume;
import com.zhilulinghang.backend.model.ReviewRecord;
import com.zhilulinghang.backend.model.ReviewRequest;
import com.zhilulinghang.backend.model.TeacherProfile;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/review-requests")
public class ReviewRequestController {
    private final ReviewRequestMapper reviewRequestMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final ResumeMapper resumeMapper;
    private final TeacherProfileMapper teacherProfileMapper;

    public ReviewRequestController(ReviewRequestMapper reviewRequestMapper, ReviewRecordMapper reviewRecordMapper, ResumeMapper resumeMapper, TeacherProfileMapper teacherProfileMapper) {
        this.reviewRequestMapper = reviewRequestMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.resumeMapper = resumeMapper;
        this.teacherProfileMapper = teacherProfileMapper;
    }

    @PostMapping
    public List<ReviewRequest> create(@RequestBody ReviewRequestCreateRequest request) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        Resume resume = requireOwnedResume(request.getSourceResumeId(), user);
        if (Boolean.TRUE.equals(resume.getFrozen())) {
            throw new IllegalStateException("该简历正在撤回处理中，暂时不能提交新的审核请求");
        }
        String assignMode = normalizeAssignMode(request.getAssignMode());
        List<Long> teacherIds = resolveTeacherIds(assignMode, request.getTeacherIds());

        List<ReviewRequest> created = new ArrayList<>();
        for (Long teacherId : teacherIds) {
            requireAvailableTeacher(teacherId);
            if (reviewRequestMapper.countNonRepeatable(resume.getId(), teacherId) > 0) {
                throw new IllegalArgumentException("该简历已经发送给该教师，请勿重复提交");
            }
            ReviewRequest reviewRequest = new ReviewRequest();
            reviewRequest.setSourceResumeId(resume.getId());
            reviewRequest.setStudentId(resume.getStudentId());
            reviewRequest.setTeacherId(teacherId);
            reviewRequest.setAssignMode(assignMode);
            reviewRequest.setStatus("PENDING");
            reviewRequest.setStudentMessage(trim(request.getStudentMessage()));
            reviewRequestMapper.insert(reviewRequest);
            created.add(reviewRequestMapper.findById(reviewRequest.getId()));
        }

        resumeMapper.submit(resume.getId());
        return created;
    }

    @GetMapping("/student")
    public List<ReviewRequest> listStudentRequests() {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        return reviewRequestMapper.findByStudentId(user.getId());
    }

    @GetMapping("/teacher")
    public List<ReviewRequest> listTeacherRequests() {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        return reviewRequestMapper.findByTeacherId(user.getId());
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        AuthUser user = AuthContext.get();
        ReviewRequest request = requireVisibleRequest(id, user);
        Resume resume = resumeMapper.findById(request.getSourceResumeId());
        return Map.of("request", request, "resume", resume);
    }

    @PostMapping("/{id}/accept")
    public ReviewRequest accept(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        ReviewRequest request = requireTeacherRequest(id, user);
        requirePending(request);
        String teacherReply = body == null ? null : trim(body.get("teacherReply"));
        reviewRequestMapper.accept(id, teacherReply);
        return reviewRequestMapper.findById(id);
    }

    @PostMapping("/{id}/decline")
    public ReviewRequest decline(@PathVariable Long id, @RequestBody ReviewRequestDeclineRequest body) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        ReviewRequest request = requireTeacherRequest(id, user);
        requirePending(request);
        if (body == null || !StringUtils.hasText(body.getDeclineReason()) || !StringUtils.hasText(body.getDeclineSuggestion())) {
            throw new IllegalArgumentException("拒绝请求时必须填写理由和建议");
        }
        reviewRequestMapper.decline(id, body.getDeclineReason().trim(), body.getDeclineSuggestion().trim());
        return reviewRequestMapper.findById(id);
    }

    @PostMapping("/{id}/return")
    public Map<String, Object> returnResume(@PathVariable Long id, @RequestBody ReviewReturnRequest body) {
        AuthUser user = requireRole("TEACHER", "ADMIN");
        ReviewRequest request = requireTeacherRequest(id, user);
        if (!"ACCEPTED".equals(request.getStatus())) {
            throw new IllegalArgumentException("只有已接受的请求可以返回新简历");
        }
        if (body == null || !StringUtils.hasText(body.getName())) {
            throw new IllegalArgumentException("返回简历至少需要填写姓名");
        }

        Resume sourceResume = resumeMapper.findById(request.getSourceResumeId());
        if (sourceResume == null) {
            throw new IllegalArgumentException("源简历不存在");
        }

        Resume returnedResume = new Resume();
        returnedResume.setStudentId(request.getStudentId());
        returnedResume.setSourceResumeId(sourceResume.getId());
        returnedResume.setGeneratedByTeacherId(request.getTeacherId());
        returnedResume.setVersionType("TEACHER_RETURNED");
        returnedResume.setTitle(defaultReturnedTitle(body.getTitle(), sourceResume.getTitle()));
        returnedResume.setName(body.getName());
        returnedResume.setPhone(body.getPhone());
        returnedResume.setEmail(body.getEmail());
        returnedResume.setTargetPosition(body.getTargetPosition());
        returnedResume.setEducation(body.getEducation());
        returnedResume.setExperience(body.getExperience());
        returnedResume.setSkills(body.getSkills());
        returnedResume.setAwards(body.getAwards());
        returnedResume.setSelfEvaluation(body.getSelfEvaluation());
        returnedResume.setStatus("DRAFT");
        returnedResume.setTeacherComment(trim(body.getComment()));
        returnedResume.setFrozen(false);
        resumeMapper.insert(returnedResume);

        reviewRequestMapper.complete(id, trim(body.getComment()));

        ReviewRecord record = new ReviewRecord();
        record.setRequestId(id);
        record.setSourceResumeId(sourceResume.getId());
        record.setReturnedResumeId(returnedResume.getId());
        record.setStudentId(request.getStudentId());
        record.setTeacherId(request.getTeacherId());
        record.setAction("RETURNED");
        record.setComment(trim(body.getComment()));
        record.setTeacherDeleted(false);
        reviewRecordMapper.insert(record);

        return Map.of(
                "request", reviewRequestMapper.findById(id),
                "resume", resumeMapper.findById(returnedResume.getId()),
                "record", record
        );
    }

    @PostMapping("/{id}/withdraw")
    public ReviewRequest withdraw(@PathVariable Long id, @RequestBody ReviewRequestWithdrawRequest body) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        ReviewRequest request = requireRequest(id);
        if (!"ADMIN".equals(user.getRole()) && !request.getStudentId().equals(user.getId())) {
            throw new IllegalStateException("不能撤回他人的审核请求");
        }
        if (!"ACCEPTED".equals(request.getStatus())) {
            throw new IllegalArgumentException("只有教师已接受的请求可以申请撤回");
        }
        if (body == null || !StringUtils.hasText(body.getReason())) {
            throw new IllegalArgumentException("申请撤回必须填写原因");
        }
        reviewRequestMapper.requestWithdraw(id, body.getReason().trim());
        resumeMapper.freeze(request.getSourceResumeId(), body.getReason().trim());
        return reviewRequestMapper.findById(id);
    }

    private List<Long> resolveTeacherIds(String assignMode, List<Long> requestedTeacherIds) {
        if ("RANDOM".equals(assignMode)) {
            TeacherProfile profile = teacherProfileMapper.findRandomAvailableApproved();
            if (profile == null) {
                throw new IllegalArgumentException("当前没有可随机分配的教师");
            }
            return List.of(profile.getTeacherId());
        }
        if (requestedTeacherIds == null || requestedTeacherIds.isEmpty()) {
            throw new IllegalArgumentException("请选择至少一位教师");
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(requestedTeacherIds);
        return new ArrayList<>(uniqueIds);
    }

    private String normalizeAssignMode(String assignMode) {
        String normalized = StringUtils.hasText(assignMode) ? assignMode.trim().toUpperCase(Locale.ROOT) : "SELECTED";
        if (!"SELECTED".equals(normalized) && !"RANDOM".equals(normalized)) {
            throw new IllegalArgumentException("分配方式不正确");
        }
        return normalized;
    }

    private void requireAvailableTeacher(Long teacherId) {
        TeacherProfile profile = teacherProfileMapper.findByTeacherId(teacherId);
        if (profile == null || !"APPROVED".equals(profile.getApprovalStatus()) || !Boolean.TRUE.equals(profile.getAvailable())) {
            throw new IllegalArgumentException("所选教师暂不可接收请求");
        }
    }

    private Resume requireOwnedResume(Long resumeId, AuthUser user) {
        if (resumeId == null) {
            throw new IllegalArgumentException("请选择简历");
        }
        Resume resume = resumeMapper.findById(resumeId);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        if (!"ADMIN".equals(user.getRole()) && !resume.getStudentId().equals(user.getId())) {
            throw new IllegalStateException("不能操作他人的简历");
        }
        return resume;
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
        throw new IllegalStateException("无权访问该资源");
    }

    private ReviewRequest requireTeacherRequest(Long id, AuthUser user) {
        ReviewRequest request = requireRequest(id);
        if (!"ADMIN".equals(user.getRole()) && !request.getTeacherId().equals(user.getId())) {
            throw new IllegalStateException("不能处理他人的审核请求");
        }
        return request;
    }

    private ReviewRequest requireRequest(Long id) {
        ReviewRequest request = reviewRequestMapper.findById(id);
        if (request == null) {
            throw new IllegalArgumentException("审核请求不存在");
        }
        return request;
    }

    private void requirePending(ReviewRequest request) {
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("只有待处理请求可以执行该操作");
        }
    }

    private String defaultReturnedTitle(String requestedTitle, String sourceTitle) {
        if (StringUtils.hasText(requestedTitle)) {
            return requestedTitle.trim();
        }
        return (StringUtils.hasText(sourceTitle) ? sourceTitle.trim() : "简历") + " - 教师返回版本";
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
