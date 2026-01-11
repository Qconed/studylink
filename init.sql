CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    fullname VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'STUDENT' CHECK (role IN ('STUDENT', 'TUTOR', 'ADMIN')),
    bio TEXT,
    avatar_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    is_suspended BOOLEAN DEFAULT FALSE,
    suspension_reason TEXT
);

CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_role ON users(role);

CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL CHECK (type IN ('ACADEMIC_SUBJECT', 'PERSONAL_INTEREST', 'TRAINING')),
    level INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_type ON categories(type);

CREATE TABLE IF NOT EXISTS user_categories (
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, category_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_user ON user_categories(user_id);
CREATE INDEX idx_category ON user_categories(category_id);

CREATE TABLE IF NOT EXISTS friend_requests (
    id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (sender_id, receiver_id, status)
);

CREATE INDEX idx_sender ON friend_requests(sender_id);
CREATE INDEX idx_receiver ON friend_requests(receiver_id);
CREATE INDEX idx_status ON friend_requests(status);

CREATE TABLE IF NOT EXISTS friendships (
    id SERIAL PRIMARY KEY,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user1_id, user2_id),
    CHECK (user1_id < user2_id)
);

CREATE INDEX idx_user1 ON friendships(user1_id);
CREATE INDEX idx_user2 ON friendships(user2_id);

CREATE TABLE IF NOT EXISTS study_sessions (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    organizer_id INT NOT NULL,
    is_tutored BOOLEAN DEFAULT FALSE,
    price NUMERIC(8,2) DEFAULT 0 CHECK (price >= 0),
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    location VARCHAR(255),
    min_participants INT DEFAULT 1 CHECK (min_participants >= 1),
    max_participants INT DEFAULT 1 CHECK (max_participants >= min_participants),
    status VARCHAR(50) DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK ( (is_tutored = TRUE AND price >= 0) OR (is_tutored = FALSE AND price = 0) ),
    CHECK (end_datetime > start_datetime)
);

CREATE INDEX idx_study_sessions_organizer ON study_sessions(organizer_id);
CREATE INDEX idx_study_sessions_status ON study_sessions(status);
CREATE INDEX idx_study_sessions_start ON study_sessions(start_datetime);

-- Liaison entre sessions et catégories (many-to-many)
CREATE TABLE IF NOT EXISTS session_categories (
    session_id INT NOT NULL,
    category_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, category_id),
    FOREIGN KEY (session_id) REFERENCES study_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_session_categories_session ON session_categories(session_id);
CREATE INDEX idx_session_categories_category ON session_categories(category_id);

-- Participants d'une session
CREATE TABLE IF NOT EXISTS session_participants (
    session_id INT NOT NULL,
    user_id INT NOT NULL,
    role VARCHAR(50) DEFAULT 'PARTICIPANT' CHECK (role IN ('ORGANIZER', 'PARTICIPANT')),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_confirmed BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (session_id, user_id),
    FOREIGN KEY (session_id) REFERENCES study_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_session_participants_session ON session_participants(session_id);
CREATE INDEX idx_session_participants_user ON session_participants(user_id);

-- Trigger helper: keep updated_at current on update (Postgres syntax)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
   RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS trg_update_study_sessions_updated_at ON study_sessions;
CREATE TRIGGER trg_update_study_sessions_updated_at
BEFORE UPDATE ON study_sessions
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();
