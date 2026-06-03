CREATE DATABASE IF NOT EXISTS zhilulinghang DEFAULT CHARACTER SET utf8mb4;
USE zhilulinghang;

CREATE TABLE job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    city VARCHAR(50),
    salary_min INT,
    salary_max INT,
    skills VARCHAR(255),
    description TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);