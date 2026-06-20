USE zhilulinghang;

DROP PROCEDURE IF EXISTS add_resume_column_if_missing;

DELIMITER //
CREATE PROCEDURE add_resume_column_if_missing(
    IN column_name_value VARCHAR(64),
    IN column_definition_value TEXT,
    IN after_column_value VARCHAR(64)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'resume'
          AND COLUMN_NAME = column_name_value
    ) THEN
        SET @sql = CONCAT(
            'ALTER TABLE resume ADD COLUMN `',
            column_name_value, '` ', column_definition_value,
            ' AFTER `', after_column_value, '`'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL add_resume_column_if_missing('source_resume_id', 'BIGINT NULL', 'student_id');
CALL add_resume_column_if_missing('generated_by_teacher_id', 'BIGINT NULL', 'source_resume_id');
CALL add_resume_column_if_missing('version_type', 'VARCHAR(30) NOT NULL DEFAULT ''ORIGINAL''', 'generated_by_teacher_id');

DROP PROCEDURE IF EXISTS create_resume_index_if_missing;

DELIMITER //
CREATE PROCEDURE create_resume_index_if_missing(
    IN index_name_value VARCHAR(64),
    IN index_definition_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'resume'
          AND INDEX_NAME = index_name_value
    ) THEN
        SET @sql = CONCAT('CREATE INDEX `', index_name_value, '` ON resume ', index_definition_value);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL create_resume_index_if_missing('idx_resume_source_id', '(source_resume_id)');

CREATE TABLE IF NOT EXISTS review_record (
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

DROP PROCEDURE IF EXISTS create_review_record_index_if_missing;

DELIMITER //
CREATE PROCEDURE create_review_record_index_if_missing(
    IN index_name_value VARCHAR(64),
    IN index_definition_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'review_record'
          AND INDEX_NAME = index_name_value
    ) THEN
        SET @sql = CONCAT('CREATE INDEX `', index_name_value, '` ON review_record ', index_definition_value);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL create_review_record_index_if_missing('idx_review_record_student', '(student_id)');
CALL create_review_record_index_if_missing('idx_review_record_teacher', '(teacher_id)');
CALL create_review_record_index_if_missing('idx_review_record_request', '(request_id)');

UPDATE resume
SET version_type = 'ORIGINAL'
WHERE version_type IS NULL OR version_type = '';

DROP PROCEDURE IF EXISTS add_resume_column_if_missing;
DROP PROCEDURE IF EXISTS create_resume_index_if_missing;
DROP PROCEDURE IF EXISTS create_review_record_index_if_missing;
