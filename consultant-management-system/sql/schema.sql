CREATE DATABASE IF NOT EXISTS consultant_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE consultant_db;

CREATE TABLE IF NOT EXISTS consultants (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    phone       VARCHAR(15)  NOT NULL,
    technology  VARCHAR(150) NOT NULL,
    experience  INT          NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_consultant_email UNIQUE (email),
    CONSTRAINT chk_experience CHECK (experience >= 0 AND experience <= 50),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_consultant_name       ON consultants (name);
CREATE INDEX idx_consultant_technology ON consultants (technology);

INSERT INTO consultants (name, email, phone, technology, experience, status, created_at) VALUES
 ('John Doe',      'john.doe@email.com',      '9876543210', 'Java, Spring Boot',       5, 'ACTIVE',   DATE_SUB(NOW(), INTERVAL 5 MONTH)),
 ('Jane Smith',    'jane.smith@email.com',    '9876543211', 'Angular, Java',           4, 'ACTIVE',   DATE_SUB(NOW(), INTERVAL 4 MONTH)),
 ('Mike Brown',    'mike.brown@email.com',    '9876543212', 'Python, Django',          6, 'ACTIVE',   DATE_SUB(NOW(), INTERVAL 4 MONTH)),
 ('Sarah Lee',     'sarah.lee@email.com',     '9876543213', 'Salesforce',              3, 'INACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
 ('David Wilson',  'david.wilson@email.com',  '9876543214', '.NET, C#',                7, 'ACTIVE',   DATE_SUB(NOW(), INTERVAL 3 MONTH)),
 ('Priya Sharma',  'priya.sharma@email.com',  '9876543215', 'React, Node.js',          4, 'ACTIVE',   DATE_SUB(NOW(), INTERVAL 2 MONTH)),
 ('Carlos Mendez', 'carlos.mendez@email.com', '9876543216', 'AWS, DevOps',             8, 'ACTIVE',   DATE_SUB(NOW(), INTERVAL 2 MONTH)),
 ('Anita Desai',   'anita.desai@email.com',   '9876543217', 'Java, Microservices',     6, 'ACTIVE',   DATE_SUB(NOW(), INTERVAL 2 MONTH)),
 ('Tom Becker',    'tom.becker@email.com',    '9876543218', 'Golang, Kubernetes',      5, 'INACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
 ('Lisa Chen',     'lisa.chen@email.com',     '9876543219', 'Data Engineering, Spark', 7, 'ACTIVE',   NOW()),
 ('Rahul Verma',   'rahul.verma@email.com',   '9876543220', 'Spring Boot, Kafka',      3, 'ACTIVE',   NOW()),
 ('Emily Clark',   'emily.clark@email.com',   '9876543221', 'QA Automation, Selenium', 4, 'ACTIVE',   NOW());