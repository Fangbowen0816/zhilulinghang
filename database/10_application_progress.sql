USE zhilulinghang;

CREATE TABLE IF NOT EXISTS application_experience (
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

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'application_experience' AND index_name = 'idx_application_experience_application') = 0,
    'CREATE INDEX idx_application_experience_application ON application_experience(application_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'application_experience' AND index_name = 'idx_application_experience_student') = 0,
    'CREATE INDEX idx_application_experience_student ON application_experience(student_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS application_reminder (
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

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'application_reminder' AND index_name = 'idx_application_reminder_application') = 0,
    'CREATE INDEX idx_application_reminder_application ON application_reminder(application_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'application_reminder' AND index_name = 'idx_application_reminder_student') = 0,
    'CREATE INDEX idx_application_reminder_student ON application_reminder(student_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'application_reminder' AND index_name = 'idx_application_reminder_status_time') = 0,
    'CREATE INDEX idx_application_reminder_status_time ON application_reminder(status, remind_time)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
