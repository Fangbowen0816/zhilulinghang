USE zhilulinghang;

DROP PROCEDURE IF EXISTS add_column_if_missing_v4;

DELIMITER //
CREATE PROCEDURE add_column_if_missing_v4(
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

CALL add_column_if_missing_v4('resume', 'frozen', 'TINYINT(1) NOT NULL DEFAULT 0', 'teacher_comment');
CALL add_column_if_missing_v4('resume', 'freeze_reason', 'TEXT NULL', 'frozen');

CALL add_column_if_missing_v4('review_request', 'withdraw_reason', 'TEXT NULL', 'decline_suggestion');
CALL add_column_if_missing_v4('review_request', 'admin_decision', 'VARCHAR(20) NULL', 'withdraw_reason');
CALL add_column_if_missing_v4('review_request', 'admin_comment', 'TEXT NULL', 'admin_decision');
CALL add_column_if_missing_v4('review_request', 'admin_id', 'BIGINT NULL', 'admin_comment');
CALL add_column_if_missing_v4('review_request', 'admin_time', 'DATETIME NULL', 'admin_id');

DROP PROCEDURE IF EXISTS add_column_if_missing_v4;
