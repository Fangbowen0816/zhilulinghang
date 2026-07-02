USE zhilulinghang;

CREATE TABLE IF NOT EXISTS resume_template (
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

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'resume_template' AND index_name = 'idx_resume_template_enabled') = 0,
    'CREATE INDEX idx_resume_template_enabled ON resume_template(enabled)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'resume_template' AND index_name = 'idx_resume_template_industry') = 0,
    'CREATE INDEX idx_resume_template_industry ON resume_template(industry)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO resume_template(name, industry, job_type, structure, style, enabled)
SELECT '经典单栏模板', '通用', '通用', 'basic,education,experience,skills,awards,selfEvaluation', 'CLASSIC', 1
WHERE NOT EXISTS (SELECT 1 FROM resume_template WHERE style = 'CLASSIC');

INSERT INTO resume_template(name, industry, job_type, structure, style, enabled)
SELECT '技术岗紧凑模板', '互联网', '技术岗', 'basic,skills,experience,education,awards,selfEvaluation', 'COMPACT', 1
WHERE NOT EXISTS (SELECT 1 FROM resume_template WHERE style = 'COMPACT');

INSERT INTO resume_template(name, industry, job_type, structure, style, enabled)
SELECT '校园清爽模板', '校园招聘', '应届生', 'basic,education,experience,awards,skills,selfEvaluation', 'MODERN', 1
WHERE NOT EXISTS (SELECT 1 FROM resume_template WHERE style = 'MODERN');

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'resume' AND column_name = 'template_id') = 0,
    'ALTER TABLE resume ADD COLUMN template_id BIGINT NULL AFTER generated_by_teacher_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE resume
SET template_id = (SELECT id FROM resume_template WHERE style = 'CLASSIC' LIMIT 1)
WHERE template_id IS NULL;
