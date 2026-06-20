CREATE DATABASE IF NOT EXISTS zhilulinghang DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zhilulinghang;

DROP TABLE IF EXISTS review_record;
DROP TABLE IF EXISTS review_request;
DROP TABLE IF EXISTS resume;
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

CREATE TABLE resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    source_resume_id BIGINT,
    generated_by_teacher_id BIGINT,
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
    CONSTRAINT fk_resume_generated_teacher FOREIGN KEY (generated_by_teacher_id) REFERENCES `user`(id)
);

CREATE INDEX idx_resume_student_id ON resume(student_id);
CREATE INDEX idx_resume_status ON resume(status);
CREATE INDEX idx_resume_source_id ON resume(source_resume_id);

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
