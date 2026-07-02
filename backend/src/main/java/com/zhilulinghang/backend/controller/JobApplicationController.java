package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ApplicationExperienceRequest;
import com.zhilulinghang.backend.dto.ApplicationReminderRequest;
import com.zhilulinghang.backend.dto.ApplicationStatusRequest;
import com.zhilulinghang.backend.mapper.ApplicationExperienceMapper;
import com.zhilulinghang.backend.mapper.ApplicationReminderMapper;
import com.zhilulinghang.backend.mapper.JobApplicationMapper;
import com.zhilulinghang.backend.model.ApplicationExperience;
import com.zhilulinghang.backend.model.ApplicationReminder;
import com.zhilulinghang.backend.model.JobApplication;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "APPLIED",
            "SCREENING",
            "WRITTEN_TEST",
            "INTERVIEW",
            "OFFER",
            "REJECTED",
            "CLOSED"
    );
    private static final Set<String> EXPERIENCE_STAGES = Set.of("WRITTEN_TEST", "INTERVIEW", "OFFER", "GENERAL");
    private static final Set<String> REMINDER_TYPES = Set.of("WRITTEN_TEST", "INTERVIEW", "FOLLOW_UP", "GENERAL");

    private final JobApplicationMapper jobApplicationMapper;
    private final ApplicationExperienceMapper applicationExperienceMapper;
    private final ApplicationReminderMapper applicationReminderMapper;

    public JobApplicationController(JobApplicationMapper jobApplicationMapper, ApplicationExperienceMapper applicationExperienceMapper, ApplicationReminderMapper applicationReminderMapper) {
        this.jobApplicationMapper = jobApplicationMapper;
        this.applicationExperienceMapper = applicationExperienceMapper;
        this.applicationReminderMapper = applicationReminderMapper;
    }

    @GetMapping("/student")
    public List<JobApplication> myApplications() {
        AuthUser user = requireRole("STUDENT");
        return jobApplicationMapper.findByStudentId(user.getId());
    }

    @GetMapping("/{id}")
    public JobApplication detail(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        JobApplication application = requireAccessibleApplication(id, user);
        return application;
    }

    @PostMapping("/{id}/status")
    public JobApplication updateStatus(@PathVariable Long id, @RequestBody ApplicationStatusRequest request) {
        AuthUser user = requireRole("STUDENT");
        JobApplication application = requireAccessibleApplication(id, user);
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("请选择投递状态");
        }
        String status = request.getStatus().trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的投递状态");
        }
        jobApplicationMapper.updateStatus(application.getId(), status);
        return jobApplicationMapper.findDetailById(application.getId());
    }

    @GetMapping("/{id}/experiences")
    public List<ApplicationExperience> experiences(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        requireAccessibleApplication(id, user);
        return applicationExperienceMapper.findByApplicationId(id);
    }

    @PostMapping("/{id}/experiences")
    public ApplicationExperience createExperience(@PathVariable Long id, @RequestBody ApplicationExperienceRequest request) {
        AuthUser user = requireRole("STUDENT");
        JobApplication application = requireAccessibleApplication(id, user);
        ApplicationExperience experience = new ApplicationExperience();
        experience.setApplicationId(application.getId());
        experience.setStudentId(user.getId());
        experience.setStage(normalizeStage(request == null ? null : request.getStage()));
        experience.setContent(requireText(request == null ? null : request.getContent(), "请填写经验记录内容"));
        applicationExperienceMapper.insert(experience);
        return applicationExperienceMapper.findById(experience.getId());
    }

    @PutMapping("/experiences/{id}")
    public ApplicationExperience updateExperience(@PathVariable Long id, @RequestBody ApplicationExperienceRequest request) {
        AuthUser user = requireRole("STUDENT");
        ApplicationExperience experience = requireOwnedExperience(id, user);
        applicationExperienceMapper.update(experience.getId(), normalizeStage(request == null ? null : request.getStage()), requireText(request == null ? null : request.getContent(), "请填写经验记录内容"));
        return applicationExperienceMapper.findById(experience.getId());
    }

    @DeleteMapping("/experiences/{id}")
    public ApplicationExperience deleteExperience(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT");
        ApplicationExperience experience = requireOwnedExperience(id, user);
        applicationExperienceMapper.deleteById(experience.getId());
        return experience;
    }

    @GetMapping("/{id}/reminders")
    public List<ApplicationReminder> reminders(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT", "ADMIN");
        requireAccessibleApplication(id, user);
        return applicationReminderMapper.findByApplicationId(id);
    }

    @PostMapping("/{id}/reminders")
    public ApplicationReminder createReminder(@PathVariable Long id, @RequestBody ApplicationReminderRequest request) {
        AuthUser user = requireRole("STUDENT");
        JobApplication application = requireAccessibleApplication(id, user);
        if (request == null || request.getRemindTime() == null) {
            throw new IllegalArgumentException("请选择提醒时间");
        }
        if (!request.getRemindTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("提醒时间必须晚于当前时间");
        }
        ApplicationReminder reminder = new ApplicationReminder();
        reminder.setApplicationId(application.getId());
        reminder.setStudentId(user.getId());
        reminder.setRemindType(normalizeReminderType(request.getRemindType()));
        reminder.setRemindTime(request.getRemindTime());
        reminder.setContent(requireText(request.getContent(), "请填写提醒内容"));
        reminder.setStatus("PENDING");
        applicationReminderMapper.insert(reminder);
        return applicationReminderMapper.findById(reminder.getId());
    }

    @PostMapping("/reminders/{id}/done")
    public ApplicationReminder markReminderDone(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT");
        ApplicationReminder reminder = requireOwnedReminder(id, user);
        applicationReminderMapper.markDone(reminder.getId());
        return applicationReminderMapper.findById(reminder.getId());
    }

    @DeleteMapping("/reminders/{id}")
    public ApplicationReminder deleteReminder(@PathVariable Long id) {
        AuthUser user = requireRole("STUDENT");
        ApplicationReminder reminder = requireOwnedReminder(id, user);
        applicationReminderMapper.deleteById(reminder.getId());
        return reminder;
    }

    @GetMapping("/student/reminders/pending")
    public List<ApplicationReminder> pendingReminders() {
        AuthUser user = requireRole("STUDENT");
        return applicationReminderMapper.findPendingByStudentId(user.getId());
    }

    private JobApplication requireAccessibleApplication(Long id, AuthUser user) {
        JobApplication application = jobApplicationMapper.findDetailById(id);
        if (application == null) {
            throw new IllegalArgumentException("投递记录不存在");
        }
        if (!"ADMIN".equals(user.getRole()) && !user.getId().equals(application.getStudentId())) {
            throw new IllegalStateException("不能访问他人的投递记录");
        }
        return application;
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

    private ApplicationExperience requireOwnedExperience(Long id, AuthUser user) {
        ApplicationExperience experience = applicationExperienceMapper.findById(id);
        if (experience == null) {
            throw new IllegalArgumentException("经验记录不存在");
        }
        if (!user.getId().equals(experience.getStudentId())) {
            throw new IllegalStateException("不能操作他人的经验记录");
        }
        return experience;
    }

    private ApplicationReminder requireOwnedReminder(Long id, AuthUser user) {
        ApplicationReminder reminder = applicationReminderMapper.findById(id);
        if (reminder == null) {
            throw new IllegalArgumentException("提醒事项不存在");
        }
        if (!user.getId().equals(reminder.getStudentId())) {
            throw new IllegalStateException("不能操作他人的提醒事项");
        }
        return reminder;
    }

    private String normalizeStage(String stage) {
        String value = normalizeOrDefault(stage, "GENERAL");
        if (!EXPERIENCE_STAGES.contains(value)) {
            throw new IllegalArgumentException("不支持的经验阶段");
        }
        return value;
    }

    private String normalizeReminderType(String remindType) {
        String value = normalizeOrDefault(remindType, "GENERAL");
        if (!REMINDER_TYPES.contains(value)) {
            throw new IllegalArgumentException("不支持的提醒类型");
        }
        return value;
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim().toUpperCase();
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
