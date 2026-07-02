USE zhilulinghang;

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content TEXT,
    read_status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    related_type VARCHAR(50),
    related_id BIGINT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_time DATETIME,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `user`(id)
);

CREATE INDEX idx_notification_user_status ON notification(user_id, read_status);
CREATE INDEX idx_notification_user_time ON notification(user_id, create_time);
CREATE INDEX idx_notification_related ON notification(related_type, related_id);
