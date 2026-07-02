USE zhilulinghang;

INSERT INTO `user` (username, password, role)
VALUES
('admin', '123456', 'ADMIN'),
('student', '123456', 'STUDENT'),
('teacher', '123456', 'TEACHER'),
('student2', '123456', 'STUDENT');

INSERT INTO platform_setting(setting_key, setting_value, description)
VALUES
('teacher_review_enabled', 'true', '是否开放学生提交教师审核请求'),
('resume_polish_enabled', 'true', '是否开放 AI 简历润色功能'),
('maintenance_notice', '', '平台维护公告');

INSERT INTO resume_template(name, industry, job_type, structure, style, enabled)
VALUES
('经典单栏模板', '通用', '通用', 'basic,education,experience,skills,awards,selfEvaluation', 'CLASSIC', 1),
('技术岗紧凑模板', '互联网', '技术岗', 'basic,skills,experience,education,awards,selfEvaluation', 'COMPACT', 1),
('校园清爽模板', '校园招聘', '应届生', 'basic,education,experience,awards,skills,selfEvaluation', 'MODERN', 1);

INSERT INTO teacher_profile(teacher_id, display_name, department, title, bio, expertise_tags, available, approval_status, approval_comment, approved_by, approved_time)
VALUES
((SELECT id FROM `user` WHERE username = 'teacher'), '王老师', '软件工程系', '讲师', '长期指导学生完善校招简历，关注项目经历表达和工程能力呈现。', 'Java 后端,Vue 前端,校招简历', 1, 'APPROVED', NULL, (SELECT id FROM `user` WHERE username = 'admin'), CURRENT_TIMESTAMP);

INSERT INTO resume(student_id, template_id, title, name, phone, email, target_position, education, experience, skills, awards, self_evaluation, status, teacher_comment)
VALUES
((SELECT id FROM `user` WHERE username = 'student'), (SELECT id FROM resume_template WHERE style = 'COMPACT' LIMIT 1), 'Java 后端校招简历', '张同学', '13800000000', 'student@example.com', 'Java 后端开发实习生', '软件工程本科，主修 Java、数据库、软件测试', '参与课程项目“职路领航”，负责前端页面和接口联调', 'Java, Spring Boot, Vue, MySQL', '校级软件设计竞赛三等奖', '学习能力强，重视代码质量和团队协作。', 'DRAFT', NULL),
((SELECT id FROM `user` WHERE username = 'student2'), (SELECT id FROM resume_template WHERE style = 'CLASSIC' LIMIT 1), '校园项目后端简历', '李同学', '13900000000', 'student2@example.com', '后端开发实习生', '计算机科学与技术本科', '完成校园二手交易平台后端接口开发', 'Java, MyBatis, Redis, Linux', '通过 CET-4', '熟悉基础后端开发流程，能够独立完成接口开发。', 'SUBMITTED', NULL),
((SELECT id FROM `user` WHERE username = 'student'), (SELECT id FROM resume_template WHERE style = 'MODERN' LIMIT 1), '前端方向已通过版本', '张同学', '13800000000', 'student@example.com', '前端开发实习生', '软件工程本科', '参与简历审核 MVP 开发', 'Vue, Element Plus, Axios', '校级优秀学生', '具备较好的页面实现和接口联调能力。', 'APPROVED', '结构清晰，可以补充项目量化成果。'),
((SELECT id FROM `user` WHERE username = 'student2'), (SELECT id FROM resume_template WHERE style = 'CLASSIC' LIMIT 1), '基础后端退回版本', '李同学', '13900000000', 'student2@example.com', 'Java 开发实习生', '计算机科学与技术本科', '项目经历描述较简单', 'Java, SQL', '无', '希望在真实项目中提升工程实践能力。', 'REJECTED', '建议补充项目职责、技术难点和最终成果。');

INSERT INTO job(title, company, industry, city, salary_range, requirement, description, status)
VALUES
('Java 后端开发实习生', '星河云科技', '互联网', '武汉', '150-220/天', '熟悉 Java、Spring Boot、MySQL，了解 RESTful API 设计；有课程项目或实习项目经验优先。', '参与企业内部协同平台后端接口开发，完成业务模块编码、接口联调和基础测试。', 'OPEN'),
('前端开发实习生', '启明数智', '互联网', '杭州', '180-260/天', '熟悉 Vue、JavaScript、HTML、CSS，了解组件化开发；具备良好的页面还原和接口联调能力。', '参与招聘数据看板、用户运营后台等 Web 应用开发，配合产品和后端完成需求迭代。', 'OPEN'),
('数据分析助理', '长江咨询', '咨询服务', '上海', '120-180/天', '熟悉 Excel 或 SQL，具备基础数据清洗、可视化和报告撰写能力。', '协助业务团队整理招聘与就业数据，输出分析报告和可视化图表。', 'OPEN'),
('测试开发实习生', '蓝桥软件', '软件服务', '深圳', '140-200/天', '了解软件测试流程，熟悉至少一种编程语言，能编写接口测试或自动化测试脚本。', '参与 Web 系统测试用例设计、缺陷跟踪、自动化脚本维护和版本质量评估。', 'CLOSED');

INSERT INTO job_application(student_id, resume_id, job_id, status)
VALUES
((SELECT id FROM `user` WHERE username = 'student'),
 (SELECT id FROM resume WHERE title = 'Java 后端校招简历' LIMIT 1),
 (SELECT id FROM job WHERE title = 'Java 后端开发实习生' LIMIT 1),
 'APPLIED');

INSERT INTO application_experience(application_id, student_id, stage, content)
VALUES
((SELECT ja.id FROM job_application ja JOIN `user` u ON ja.student_id = u.id JOIN job j ON ja.job_id = j.id WHERE u.username = 'student' AND j.title = 'Java 后端开发实习生' LIMIT 1),
 (SELECT id FROM `user` WHERE username = 'student'),
 'GENERAL',
 '已投递，后续准备复盘项目经历中的技术难点和量化成果。');

INSERT INTO application_reminder(application_id, student_id, remind_type, remind_time, content, status)
VALUES
((SELECT ja.id FROM job_application ja JOIN `user` u ON ja.student_id = u.id JOIN job j ON ja.job_id = j.id WHERE u.username = 'student' AND j.title = 'Java 后端开发实习生' LIMIT 1),
 (SELECT id FROM `user` WHERE username = 'student'),
 'FOLLOW_UP',
 DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
 '跟进 Java 后端开发实习生投递进展',
 'PENDING');

INSERT INTO review_request(source_resume_id, student_id, teacher_id, assign_mode, status, student_message, teacher_reply)
VALUES
((SELECT id FROM resume WHERE student_id = (SELECT id FROM `user` WHERE username = 'student') ORDER BY id LIMIT 1),
 (SELECT id FROM `user` WHERE username = 'student'),
 (SELECT id FROM `user` WHERE username = 'teacher'),
 'SELECTED',
 'ACCEPTED',
 'Please review my backend resume.',
 'Accepted. I will focus on project details and skills.');

INSERT INTO resume_annotation(request_id, resume_id, teacher_id, student_id, field_name, mark_type, content)
VALUES
((SELECT id FROM review_request WHERE student_message = 'Please review my backend resume.' LIMIT 1),
 (SELECT source_resume_id FROM review_request WHERE student_message = 'Please review my backend resume.' LIMIT 1),
 (SELECT id FROM `user` WHERE username = 'teacher'),
 (SELECT id FROM `user` WHERE username = 'student'),
 'experience',
 'STRUCTURE',
 'Add concrete responsibility, technical difficulty, and measurable result for the main project.'),
((SELECT id FROM review_request WHERE student_message = 'Please review my backend resume.' LIMIT 1),
 (SELECT source_resume_id FROM review_request WHERE student_message = 'Please review my backend resume.' LIMIT 1),
 (SELECT id FROM `user` WHERE username = 'teacher'),
 (SELECT id FROM `user` WHERE username = 'student'),
 'skills',
 'KEYWORD',
 'Group skills by backend, database, frontend, and tools.');

INSERT INTO resume_score(request_id, resume_id, teacher_id, student_id, score, remark)
VALUES
((SELECT id FROM review_request WHERE student_message = 'Please review my backend resume.' LIMIT 1),
 (SELECT source_resume_id FROM review_request WHERE student_message = 'Please review my backend resume.' LIMIT 1),
 (SELECT id FROM `user` WHERE username = 'teacher'),
 (SELECT id FROM `user` WHERE username = 'student'),
 82,
 'Solid base. Improve quantified outcomes and project narrative.');
