USE zhilulinghang;

DROP PROCEDURE IF EXISTS add_column_if_missing;

DELIMITER //
CREATE PROCEDURE add_column_if_missing(
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64),
    IN column_definition_value TEXT,
    IN after_column_value VARCHAR(64)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = table_name_value
          AND COLUMN_NAME = column_name_value
    ) THEN
        SET @sql = CONCAT(
            'ALTER TABLE `', table_name_value, '` ADD COLUMN `',
            column_name_value, '` ', column_definition_value,
            ' AFTER `', after_column_value, '`'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL add_column_if_missing('resume', 'title', 'VARCHAR(80) NOT NULL DEFAULT ''未命名简历''', 'student_id');
CALL add_column_if_missing('resume', 'phone', 'VARCHAR(100) NULL', 'name');
CALL add_column_if_missing('resume', 'email', 'VARCHAR(100) NULL', 'phone');
CALL add_column_if_missing('resume', 'target_position', 'VARCHAR(100) NULL', 'email');
CALL add_column_if_missing('resume', 'awards', 'TEXT NULL', 'skills');
CALL add_column_if_missing('resume', 'self_evaluation', 'TEXT NULL', 'awards');

UPDATE resume
SET title = name
WHERE title = '未命名简历' OR title IS NULL OR title = '';

DROP PROCEDURE IF EXISTS add_column_if_missing;
