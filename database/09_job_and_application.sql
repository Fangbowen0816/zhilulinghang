USE zhilulinghang;

CREATE TABLE IF NOT EXISTS job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    company VARCHAR(120),
    industry VARCHAR(80),
    city VARCHAR(80),
    salary_range VARCHAR(80),
    requirement TEXT,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'company') = 0,
    'ALTER TABLE job ADD COLUMN company VARCHAR(120) AFTER title',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'industry') = 0,
    'ALTER TABLE job ADD COLUMN industry VARCHAR(80) AFTER company',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'salary_range') = 0,
    'ALTER TABLE job ADD COLUMN salary_range VARCHAR(80) AFTER city',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'requirement') = 0,
    'ALTER TABLE job ADD COLUMN requirement TEXT AFTER salary_range',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'create_time') = 0,
    'ALTER TABLE job ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'update_time') = 0,
    'ALTER TABLE job ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'company_name') > 0,
    'ALTER TABLE job MODIFY company_name VARCHAR(120) NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'company_name') > 0,
    'UPDATE job SET company = company_name WHERE company IS NULL OR company = ''''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'skills') > 0,
    'UPDATE job SET requirement = skills WHERE requirement IS NULL OR requirement = ''''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'salary_min') > 0
              AND (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'job' AND column_name = 'salary_max') > 0,
    'UPDATE job SET salary_range = CONCAT(COALESCE(salary_min, 0), ''-'', COALESCE(salary_max, 0)) WHERE salary_range IS NULL OR salary_range = ''''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'job' AND index_name = 'idx_job_status') = 0,
    'CREATE INDEX idx_job_status ON job(status)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'job' AND index_name = 'idx_job_industry') = 0,
    'CREATE INDEX idx_job_industry ON job(industry)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'job' AND index_name = 'idx_job_city') = 0,
    'CREATE INDEX idx_job_city ON job(city)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS job_application (
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

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'job_application' AND index_name = 'idx_job_application_student') = 0,
    'CREATE INDEX idx_job_application_student ON job_application(student_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'job_application' AND index_name = 'idx_job_application_job') = 0,
    'CREATE INDEX idx_job_application_job ON job_application(job_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'job_application' AND index_name = 'idx_job_application_status') = 0,
    'CREATE INDEX idx_job_application_status ON job_application(status)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO job(title, company, industry, city, salary_range, requirement, description, status)
SELECT 'Java 后端开发实习生', '星河云科技', '互联网', '武汉', '150-220/天', '熟悉 Java、Spring Boot、MySQL，了解 RESTful API 设计；有课程项目或实习项目经验优先。', '参与企业内部协同平台后端接口开发，完成业务模块编码、接口联调和基础测试。', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM job WHERE title = 'Java 后端开发实习生' AND company = '星河云科技');

INSERT INTO job(title, company, industry, city, salary_range, requirement, description, status)
SELECT '前端开发实习生', '启明数智', '互联网', '杭州', '180-260/天', '熟悉 Vue、JavaScript、HTML、CSS，了解组件化开发；具备良好的页面还原和接口联调能力。', '参与招聘数据看板、用户运营后台等 Web 应用开发，配合产品和后端完成需求迭代。', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM job WHERE title = '前端开发实习生' AND company = '启明数智');

INSERT INTO job(title, company, industry, city, salary_range, requirement, description, status)
SELECT '数据分析助理', '长江咨询', '咨询服务', '上海', '120-180/天', '熟悉 Excel 或 SQL，具备基础数据清洗、可视化和报告撰写能力。', '协助业务团队整理招聘与就业数据，输出分析报告和可视化图表。', 'OPEN'
WHERE NOT EXISTS (SELECT 1 FROM job WHERE title = '数据分析助理' AND company = '长江咨询');

INSERT INTO job(title, company, industry, city, salary_range, requirement, description, status)
SELECT '测试开发实习生', '蓝桥软件', '软件服务', '深圳', '140-200/天', '了解软件测试流程，熟悉至少一种编程语言，能编写接口测试或自动化测试脚本。', '参与 Web 系统测试用例设计、缺陷跟踪、自动化脚本维护和版本质量评估。', 'CLOSED'
WHERE NOT EXISTS (SELECT 1 FROM job WHERE title = '测试开发实习生' AND company = '蓝桥软件');
