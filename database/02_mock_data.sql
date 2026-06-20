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

INSERT INTO teacher_profile(teacher_id, display_name, department, title, bio, expertise_tags, available, approval_status, approval_comment, approved_by, approved_time)
VALUES
((SELECT id FROM `user` WHERE username = 'teacher'), '王老师', '软件工程系', '讲师', '长期指导学生完善校招简历，关注项目经历表达和工程能力呈现。', 'Java 后端,Vue 前端,校招简历', 1, 'APPROVED', NULL, (SELECT id FROM `user` WHERE username = 'admin'), CURRENT_TIMESTAMP);

INSERT INTO resume(student_id, title, name, phone, email, target_position, education, experience, skills, awards, self_evaluation, status, teacher_comment)
VALUES
((SELECT id FROM `user` WHERE username = 'student'), 'Java 后端校招简历', '张同学', '13800000000', 'student@example.com', 'Java 后端开发实习生', '软件工程本科，主修 Java、数据库、软件测试', '参与课程项目“职路领航”，负责前端页面和接口联调', 'Java, Spring Boot, Vue, MySQL', '校级软件设计竞赛三等奖', '学习能力强，重视代码质量和团队协作。', 'DRAFT', NULL),
((SELECT id FROM `user` WHERE username = 'student2'), '校园项目后端简历', '李同学', '13900000000', 'student2@example.com', '后端开发实习生', '计算机科学与技术本科', '完成校园二手交易平台后端接口开发', 'Java, MyBatis, Redis, Linux', '通过 CET-4', '熟悉基础后端开发流程，能够独立完成接口开发。', 'SUBMITTED', NULL),
((SELECT id FROM `user` WHERE username = 'student'), '前端方向已通过版本', '张同学', '13800000000', 'student@example.com', '前端开发实习生', '软件工程本科', '参与简历审核 MVP 开发', 'Vue, Element Plus, Axios', '校级优秀学生', '具备较好的页面实现和接口联调能力。', 'APPROVED', '结构清晰，可以补充项目量化成果。'),
((SELECT id FROM `user` WHERE username = 'student2'), '基础后端退回版本', '李同学', '13900000000', 'student2@example.com', 'Java 开发实习生', '计算机科学与技术本科', '项目经历描述较简单', 'Java, SQL', '无', '希望在真实项目中提升工程实践能力。', 'REJECTED', '建议补充项目职责、技术难点和最终成果。');
