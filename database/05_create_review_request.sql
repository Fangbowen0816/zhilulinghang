USE zhilulinghang;

CREATE TABLE IF NOT EXISTS review_request (
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
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_request_resume FOREIGN KEY (source_resume_id) REFERENCES resume(id),
    CONSTRAINT fk_review_request_student FOREIGN KEY (student_id) REFERENCES `user`(id),
    CONSTRAINT fk_review_request_teacher FOREIGN KEY (teacher_id) REFERENCES `user`(id)
);

DROP PROCEDURE IF EXISTS create_review_request_index_if_missing;

DELIMITER //
CREATE PROCEDURE create_review_request_index_if_missing(
    IN index_name_value VARCHAR(64),
    IN index_definition_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'review_request'
          AND INDEX_NAME = index_name_value
    ) THEN
        SET @sql = CONCAT('CREATE INDEX `', index_name_value, '` ON review_request ', index_definition_value);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL create_review_request_index_if_missing('idx_review_request_student', '(student_id)');
CALL create_review_request_index_if_missing('idx_review_request_teacher', '(teacher_id)');
CALL create_review_request_index_if_missing('idx_review_request_source_teacher', '(source_resume_id, teacher_id)');
CALL create_review_request_index_if_missing('idx_review_request_status', '(status)');

DROP PROCEDURE IF EXISTS create_review_request_index_if_missing;
