CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fullname VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* Utilisateur test: fullname=admin, password=test123 */
INSERT INTO users (fullname, password, email)
VALUES ('admin', 'cc03e747a6afbbcbf8be7668acfebee5', 'admin@test.com');
