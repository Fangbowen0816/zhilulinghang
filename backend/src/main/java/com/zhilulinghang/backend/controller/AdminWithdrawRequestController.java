package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.ReviewRequestWithdrawRequest;
import com.zhilulinghang.backend.mapper.AdminActionLogMapper;
import com.zhilulinghang.backend.mapper.ResumeMapper;
import com.zhilulinghang.backend.mapper.ReviewRequestMapper;
import com.zhilulinghang.backend.model.AdminActionLog;
import com.zhilulinghang.backend.model.ReviewRequest;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/withdraw-requests")
public class AdminWithdrawRequestController {
    private final ReviewRequestMapper reviewRequestMapper;
    private final ResumeMapper resumeMapper;
    private final AdminActionLogMapper adminActionLogMapper;

    public AdminWithdrawRequestController(ReviewRequestMapper reviewRequestMapper, ResumeMapper resumeMapper, AdminActionLogMapper adminActionLogMapper) {
        this.reviewRequestMapper = reviewRequestMapper;
        this.resumeMapper = resumeMapper;
        this.adminActionLogMapper = adminActionLogMapper;
    }

    @GetMapping
    public List<ReviewRequest> listPending() {
        requireAdmin();
        return reviewRequestMapper.findWithdrawPending();
    }

    @PostMapping("/{id}/approve")
    public ReviewRequest approve(@PathVariable Long id, @RequestBody(required = false) ReviewRequestWithdrawRequest body) {
        AuthUser admin = requireAdmin();
        ReviewRequest request = requireWithdrawPending(id);
        reviewRequestMapper.approveWithdraw(id, admin.getId(), body == null ? null : trim(body.getAdminComment()));
        resumeMapper.unfreeze(request.getSourceResumeId());
        log(admin, "APPROVE_WITHDRAW_REQUEST", "REVIEW_REQUEST", id, "sourceResumeId=" + request.getSourceResumeId());
        return reviewRequestMapper.findById(id);
    }

    @PostMapping("/{id}/reject")
    public ReviewRequest reject(@PathVariable Long id, @RequestBody ReviewRequestWithdrawRequest body) {
        AuthUser admin = requireAdmin();
        ReviewRequest request = requireWithdrawPending(id);
        if (body == null || !StringUtils.hasText(body.getAdminComment())) {
            throw new IllegalArgumentException("拒绝撤回时必须填写处理意见");
        }
        reviewRequestMapper.rejectWithdraw(id, admin.getId(), body.getAdminComment().trim());
        resumeMapper.unfreeze(request.getSourceResumeId());
        log(admin, "REJECT_WITHDRAW_REQUEST", "REVIEW_REQUEST", id, "sourceResumeId=" + request.getSourceResumeId());
        return reviewRequestMapper.findById(id);
    }

    private ReviewRequest requireWithdrawPending(Long id) {
        ReviewRequest request = reviewRequestMapper.findById(id);
        if (request == null) {
            throw new IllegalArgumentException("撤回申请不存在");
        }
        if (!"WITHDRAW_PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("只有待管理员处理的撤回申请可以操作");
        }
        return request;
    }

    private AuthUser requireAdmin() {
        AuthUser user = AuthContext.get();
        if (!"ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("无权访问该资源");
        }
        return user;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void log(AuthUser admin, String action, String targetType, Long targetId, String detail) {
        AdminActionLog log = new AdminActionLog();
        log.setAdminId(admin.getId());
        log.setAdminUsername(admin.getUsername());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        adminActionLogMapper.insert(log);
    }
}
