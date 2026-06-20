USE zhilulinghang;

ALTER TABLE `user`
    ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1 AFTER role;

CREATE TABLE IF NOT EXISTS platform_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(80) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(255),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    admin_username VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_action_log_admin FOREIGN KEY (admin_id) REFERENCES `user`(id)
);

CREATE INDEX idx_admin_action_log_admin ON admin_action_log(admin_id);
CREATE INDEX idx_admin_action_log_action ON admin_action_log(action);
CREATE INDEX idx_admin_action_log_create_time ON admin_action_log(create_time);

INSERT INTO platform_setting(setting_key, setting_value, description)
VALUES
('teacher_review_enabled', 'true', '是否开放学生提交教师审核请求'),
('resume_polish_enabled', 'true', '是否开放 AI 简历润色功能'),
('maintenance_notice', '', '平台维护公告')
ON DUPLICATE KEY UPDATE description = VALUES(description);
