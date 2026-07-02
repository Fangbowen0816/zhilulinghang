package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.JobApplyRequest;
import com.zhilulinghang.backend.mapper.JobApplicationMapper;
import com.zhilulinghang.backend.mapper.JobMapper;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.model.Job;
import com.zhilulinghang.backend.model.JobApplication;
import com.zhilulinghang.backend.model.Resume;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final JobApplicationMapper jobApplicationMapper;

    public JobController(JobMapper jobMapper, ResumeMapper resumeMapper, JobApplicationMapper jobApplicationMapper) {
        this.jobMapper = jobMapper;
        this.resumeMapper = resumeMapper;
        this.jobApplicationMapper = jobApplicationMapper;
    }

    @GetMapping
    public List<Job> list(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String industry,
                          @RequestParam(required = false) String city,
                          @RequestParam(required = false) String status) {
        requireRole("STUDENT", "ADMIN");
        return jobMapper.search(trim(keyword), trim(industry), trim(city), trim(status));
    }

    @GetMapping("/{id}")
    public Job detail(@PathVariable Long id) {
        requireRole("STUDENT", "ADMIN");
        Job job = jobMapper.findById(id);
        if (job == null) {
            throw new IllegalArgumentException("岗位不存在");
        }
        return job;
    }

    @PostMapping("/{id}/apply")
    public JobApplication apply(@PathVariable Long id, @RequestBody JobApplyRequest request) {
        AuthUser user = requireRole("STUDENT");
        if (request == null || request.getResumeId() == null) {
            throw new IllegalArgumentException("请选择用于投递的简历");
        }
        Job job = jobMapper.findById(id);
        if (job == null) {
            throw new IllegalArgumentException("岗位不存在");
        }
        if (!"OPEN".equals(job.getStatus())) {
            throw new IllegalStateException("该岗位已关闭，不能投递");
        }
        Resume resume = resumeMapper.findById(request.getResumeId());
        if (resume == null || !user.getId().equals(resume.getStudentId())) {
            throw new IllegalStateException("只能使用自己的简历投递岗位");
        }
        if (jobApplicationMapper.findByStudentAndJob(user.getId(), id) != null) {
            throw new IllegalStateException("你已经投递过该岗位");
        }

        JobApplication application = new JobApplication();
        application.setStudentId(user.getId());
        application.setResumeId(resume.getId());
        application.setJobId(id);
        application.setStatus("APPLIED");
        jobApplicationMapper.insert(application);
        return jobApplicationMapper.findDetailById(application.getId());
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
