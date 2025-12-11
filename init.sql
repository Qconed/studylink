CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* Utilisateur test: username=admin, password=test123 */
INSERT INTO users (username, password, email) 
VALUES ('admin', 'cc03e747a6afbbcbf8be7668acfebee5', 'admin@test.com');
