USE zhilulinghang;

CREATE TABLE IF NOT EXISTS resume_annotation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    mark_type VARCHAR(30) NOT NULL DEFAULT 'TEXT',
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_annotation_request FOREIGN KEY (request_id) REFERENCES review_request(id),
    CONSTRAINT fk_resume_annotation_resume FOREIGN KEY (resume_id) REFERENCES resume(id),
    CONSTRAINT fk_resume_annotation_teacher FOREIGN KEY (teacher_id) REFERENCES `user`(id),
    CONSTRAINT fk_resume_annotation_student FOREIGN KEY (student_id) REFERENCES `user`(id)
);

CREATE INDEX idx_resume_annotation_request ON resume_annotation(request_id);
CREATE INDEX idx_resume_annotation_teacher ON resume_annotation(teacher_id);
CREATE INDEX idx_resume_annotation_student ON resume_annotation(student_id);
CREATE INDEX idx_resume_annotation_field ON resume_annotation(field_name);

CREATE TABLE IF NOT EXISTS resume_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL UNIQUE,
    resume_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score INT NOT NULL,
    remark TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_resume_score_range CHECK (score >= 0 AND score <= 100),
    CONSTRAINT fk_resume_score_request FOREIGN KEY (request_id) REFERENCES review_request(id),
    CONSTRAINT fk_resume_score_resume FOREIGN KEY (resume_id) REFERENCES resume(id),
    CONSTRAINT fk_resume_score_teacher FOREIGN KEY (teacher_id) REFERENCES `user`(id),
    CONSTRAINT fk_resume_score_student FOREIGN KEY (student_id) REFERENCES `user`(id)
);

CREATE INDEX idx_resume_score_teacher ON resume_score(teacher_id);
CREATE INDEX idx_resume_score_student ON resume_score(student_id);
