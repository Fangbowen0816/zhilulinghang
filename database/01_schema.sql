CREATE DATABASE IF NOT EXISTS zhilulinghang DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zhilulinghang;

DROP TABLE IF EXISTS resume_score;
DROP TABLE IF EXISTS resume_annotation;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS review_record;
DROP TABLE IF EXISTS review_request;
DROP TABLE IF EXISTS application_reminder;
DROP TABLE IF EXISTS application_experience;
DROP TABLE IF EXISTS job_application;
DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS resume;
DROP TABLE IF EXISTS resume_template;
DROP TABLE IF EXISTS teacher_profile;
DROP TABLE IF EXISTS admin_action_log;
DROP TABLE IF EXISTS platform_setting;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(80) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(255),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE admin_action_log (
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

CREATE TABLE resume_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    industry VARCHAR(80),
    job_type VARCHAR(80),
    structure TEXT,
    style VARCHAR(30) NOT NULL DEFAULT 'CLASSIC',
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_resume_template_enabled ON resume_template(enabled);
CREATE INDEX idx_resume_template_industry ON resume_template(industry);

CREATE TABLE resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    source_resume_id BIGINT,
    generated_by_teacher_id BIGINT,
    template_id BIGINT,
    version_type VARCHAR(30) NOT NULL DEFAULT 'ORIGINAL',
    title VARCHAR(80) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(100),
    email VARCHAR(100),
    target_position VARCHAR(100),
    education TEXT,
    experience TEXT,
    skills TEXT,
    awards TEXT,
    self_evaluation TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    teacher_comment TEXT,
    frozen TINYINT(1) NOT NULL DEFAULT 0,
    freeze_reason TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_student FOREIGN KEY (student_id) REFERENCES `user`(id),
    CONSTRAINT fk_resume_source FOREIGN KEY (source_resume_id) REFERENCES resume(id),
    CONSTRAINT fk_resume_generated_teacher FOREIGN KEY (generated_by_teacher_id) REFERENCES `user`(id),
    CONSTRAINT fk_resume_template FOREIGN KEY (template_id) REFERENCES resume_template(id)
);

CREATE INDEX idx_resume_student_id ON resume(student_id);
CREATE INDEX idx_resume_status ON resume(status);
CREATE INDEX idx_resume_source_id ON resume(source_resume_id);

CREATE TABLE job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    company VARCHAR(120) NOT NULL,
    industry VARCHAR(80),
    city VARCHAR(80),
    salary_range VARCHAR(80),
    requirement TEXT,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_job_status ON job(status);
CREATE INDEX idx_job_industry ON job(industry);
CREATE INDEX idx_job_city ON job(city);

CREATE TABLE job_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_application_student FOREIGN KEY (student_id) REFERENCES `user`(id),
    CONSTRAINT fk_job_application_resume FOREIGN KEY (resume_id) REFERENCES resume(id),
    CONSTRAINT fk_job_application_job FOREIGN KEY (job_id) REFERENCES job(id),
    CONSTRAINT uk_job_application_student_job UNIQUE (student_id, job_id)
);

CREATE INDEX idx_job_application_student ON job_application(student_id);
CREATE INDEX idx_job_application_job ON job_application(job_id);
CREATE INDEX idx_job_application_status ON job_application(status);

CREATE TABLE application_experience (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    stage VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_experience_application FOREIGN KEY (application_id) REFERENCES job_application(id),
    CONSTRAINT fk_application_experience_student FOREIGN KEY (student_id) REFERENCES `user`(id)
);

CREATE INDEX idx_application_experience_application ON application_experience(application_id);
CREATE INDEX idx_application_experience_student ON application_experience(student_id);

CREATE TABLE application_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    remind_type VARCHAR(30) NOT NULL,
    remind_time DATETIME NOT NULL,
    content VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_reminder_application FOREIGN KEY (application_id) REFERENCES job_application(id),
    CONSTRAINT fk_application_reminder_student FOREIGN KEY (student_id) REFERENCES `user`(id)
);

CREATE INDEX idx_application_reminder_application ON application_reminder(application_id);
CREATE INDEX idx_application_reminder_student ON application_reminder(student_id);
CREATE INDEX idx_application_reminder_status_time ON application_reminder(status, remind_time);

CREATE TABLE teacher_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    title VARCHAR(100),
    bio TEXT,
    expertise_tags VARCHAR(255),
    available TINYINT(1) NOT NULL DEFAULT 0,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approval_comment TEXT,
    approved_by BIGINT,
    approved_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_teacher_profile_user FOREIGN KEY (teacher_id) REFERENCES `user`(id),
    CONSTRAINT fk_teacher_profile_approved_by FOREIGN KEY (approved_by) REFERENCES `user`(id)
);

CREATE INDEX idx_teacher_profile_status ON teacher_profile(approval_status);
CREATE INDEX idx_teacher_profile_available ON teacher_profile(available);

CREATE TABLE review_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_resume_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    assign_mode VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    student_message TEXT,
    teacher_reply TEXT,
    decline_reason TEXT,
    decline_suggestion TEXT,
    withdraw_reason TEXT,
    admin_decision VARCHAR(20),
    admin_comment TEXT,
    admin_id BIGINT,
    admin_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_request_resume FOREIGN KEY (source_resume_id) REFERENCES resume(id),
    CONSTRAINT fk_review_request_student FOREIGN KEY (student_id) REFERENCES `user`(id),
    CONSTRAINT fk_review_request_teacher FOREIGN KEY (teacher_id) REFERENCES `user`(id),
    CONSTRAINT fk_review_request_admin FOREIGN KEY (admin_id) REFERENCES `user`(id)
);

CREATE INDEX idx_review_request_student ON review_request(student_id);
CREATE INDEX idx_review_request_teacher ON review_request(teacher_id);
CREATE INDEX idx_review_request_source_teacher ON review_request(source_resume_id, teacher_id);
CREATE INDEX idx_review_request_status ON review_request(status);

CREATE TABLE review_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    source_resume_id BIGINT NOT NULL,
    returned_resume_id BIGINT,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    comment TEXT,
    teacher_deleted TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_record_request FOREIGN KEY (request_id) REFERENCES review_request(id),
    CONSTRAINT fk_review_record_source_resume FOREIGN KEY (source_resume_id) REFERENCES resume(id),
    CONSTRAINT fk_review_record_returned_resume FOREIGN KEY (returned_resume_id) REFERENCES resume(id),
    CONSTRAINT fk_review_record_student FOREIGN KEY (student_id) REFERENCES `user`(id),
    CONSTRAINT fk_review_record_teacher FOREIGN KEY (teacher_id) REFERENCES `user`(id)
);

CREATE INDEX idx_review_record_student ON review_record(student_id);
CREATE INDEX idx_review_record_teacher ON review_record(teacher_id);
CREATE INDEX idx_review_record_request ON review_record(request_id);

CREATE TABLE resume_annotation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    mark_type VARCHAR(30) NOT NULL DEFAULT 'TEXT',
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_annotation_request FOREIGN KEY (request_id) REFERENCES review_request(id),
    CONSTRAINT fk_resume_annotation_resume FOREIGN KEY (resume_id) REFERENCES resume(id),
    CONSTRAINT fk_resume_annotation_teacher FOREIGN KEY (teacher_id) REFERENCES `user`(id),
    CONSTRAINT fk_resume_annotation_student FOREIGN KEY (student_id) REFERENCES `user`(id)
);

CREATE INDEX idx_resume_annotation_request ON resume_annotation(request_id);
CREATE INDEX idx_resume_annotation_teacher ON resume_annotation(teacher_id);
CREATE INDEX idx_resume_annotation_student ON resume_annotation(student_id);
CREATE INDEX idx_resume_annotation_field ON resume_annotation(field_name);

CREATE TABLE resume_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL UNIQUE,
    resume_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score INT NOT NULL,
    remark TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_resume_score_range CHECK (score >= 0 AND score <= 100),
    CONSTRAINT fk_resume_score_request FOREIGN KEY (request_id) REFERENCES review_request(id),
    CONSTRAINT fk_resume_score_resume FOREIGN KEY (resume_id) REFERENCES resume(id),
    CONSTRAINT fk_resume_score_teacher FOREIGN KEY (teacher_id) REFERENCES `user`(id),
    CONSTRAINT fk_resume_score_student FOREIGN KEY (student_id) REFERENCES `user`(id)
);

CREATE INDEX idx_resume_score_teacher ON resume_score(teacher_id);
CREATE INDEX idx_resume_score_student ON resume_score(student_id);

CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content TEXT,
    read_status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    related_type VARCHAR(50),
    related_id BIGINT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_time DATETIME,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `user`(id)
);

CREATE INDEX idx_notification_user_status ON notification(user_id, read_status);
CREATE INDEX idx_notification_user_time ON notification(user_id, create_time);
CREATE INDEX idx_notification_related ON notification(related_type, related_id);
