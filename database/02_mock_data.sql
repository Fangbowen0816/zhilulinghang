USE zhilulinghang;

INSERT INTO `user` (username, password, role)
VALUES
('admin', '123456', 'ADMIN'),
('student', '123456', 'STUDENT'),
('teacher', '123456', 'TEACHER'),
('student2', '123456', 'STUDENT');

INSERT INTO resume(student_id, name, education, experience, skills, status, teacher_comment)
VALUES
((SELECT id FROM `user` WHERE username = 'student'), '张同学', '软件工程本科，主修 Java、数据库、软件测试', '参与课程项目“职路领航”，负责前端页面和接口联调', 'Java, Spring Boot, Vue, MySQL', 'DRAFT', NULL),
((SELECT id FROM `user` WHERE username = 'student2'), '李同学', '计算机科学与技术本科', '完成校园二手交易平台后端接口开发', 'Java, MyBatis, Redis, Linux', 'SUBMITTED', NULL),
((SELECT id FROM `user` WHERE username = 'student'), '张同学-已通过版本', '软件工程本科', '参与简历审核 MVP 开发', 'Vue, Element Plus, Axios', 'APPROVED', '结构清晰，可以补充项目量化成果。'),
((SELECT id FROM `user` WHERE username = 'student2'), '李同学-退回版本', '计算机科学与技术本科', '项目经历描述较简单', 'Java, SQL', 'REJECTED', '建议补充项目职责、技术难点和最终成果。');
