CREATE DATABASE IF NOT EXISTS zhilulinghang DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zhilulinghang;

DROP TABLE IF EXISTS resume;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
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
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_student FOREIGN KEY (student_id) REFERENCES `user`(id)
);

CREATE INDEX idx_resume_student_id ON resume(student_id);
CREATE INDEX idx_resume_status ON resume(status);
