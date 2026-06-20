USE zhilulinghang;

CREATE TABLE IF NOT EXISTS teacher_profile (
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

DROP PROCEDURE IF EXISTS create_index_if_missing;

DELIMITER //
CREATE PROCEDURE create_index_if_missing(
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64),
    IN index_definition_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name_value
          AND INDEX_NAME = index_name_value
    ) THEN
        SET @sql = CONCAT('CREATE INDEX `', index_name_value, '` ON `', table_name_value, '` ', index_definition_value);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL create_index_if_missing('teacher_profile', 'idx_teacher_profile_status', '(approval_status)');
CALL create_index_if_missing('teacher_profile', 'idx_teacher_profile_available', '(available)');

INSERT INTO teacher_profile(teacher_id, display_name, department, title, bio, expertise_tags, available, approval_status, approval_comment, approved_by, approved_time)
SELECT u.id, '王老师', '软件工程系', '讲师', '长期指导学生完善校招简历，关注项目经历表达和工程能力呈现。', 'Java 后端,Vue 前端,校招简历', 1, 'APPROVED', NULL, admin_user.id, CURRENT_TIMESTAMP
FROM `user` u
JOIN `user` admin_user ON admin_user.username = 'admin'
WHERE u.username = 'teacher'
  AND NOT EXISTS (SELECT 1 FROM teacher_profile p WHERE p.teacher_id = u.id);

DROP PROCEDURE IF EXISTS create_index_if_missing;
