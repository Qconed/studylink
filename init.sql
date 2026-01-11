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

CREATE TABLE IF NOT EXISTS resources (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    attachment_path VARCHAR(500),
    price DECIMAL(10, 2) DEFAULT 0.00,
    owner_id INT NOT NULL,
    view_count INT DEFAULT 0,
    save_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_resources_owner ON resources(owner_id);
CREATE INDEX idx_resources_created ON resources(created_at DESC);
CREATE INDEX idx_resources_price ON resources(price);


CREATE TABLE IF NOT EXISTS resource_categories (
    resource_id INT NOT NULL,
    category_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resource_id, category_id),
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_res_cat_resource ON resource_categories(resource_id);
CREATE INDEX idx_res_cat_category ON resource_categories(category_id);


CREATE TABLE IF NOT EXISTS comments (
    id SERIAL PRIMARY KEY,
    content TEXT NOT NULL CHECK (LENGTH(content) >= 1 AND LENGTH(content) <= 500),
    author_id INT NOT NULL,
    resource_id INT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_resource ON comments(resource_id);
CREATE INDEX idx_comments_author ON comments(author_id);
CREATE INDEX idx_comments_timestamp ON comments(timestamp DESC);


CREATE TABLE IF NOT EXISTS saved_resources (
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, resource_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE
);
-- Création de la table des posts sociaux
CREATE TABLE IF NOT EXISTS posts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    likes INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_post FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Création de la table des commentaires sur les posts
CREATE TABLE IF NOT EXISTS post_comments (
    id SERIAL PRIMARY KEY,
    post_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_comment FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_comment FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_post_comments_post ON post_comments(post_id);
CREATE INDEX idx_post_comments_user ON post_comments(user_id);

-- Création de la table des likes sur les posts
CREATE TABLE IF NOT EXISTS post_likes (
    user_id INTEGER NOT NULL,
    post_id INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

CREATE INDEX idx_post_likes_post ON post_likes(post_id);
CREATE INDEX idx_post_likes_user ON post_likes(user_id);

-- Création de la table du panier
CREATE TABLE IF NOT EXISTS cart_items (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    resource_id INTEGER,
    session_id INTEGER,
    item_type VARCHAR(20) NOT NULL CHECK (item_type IN ('RESOURCE', 'SESSION')),
    quantity INTEGER DEFAULT 1,
    added_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_resource FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_session FOREIGN KEY (session_id) REFERENCES tutor_sessions(id) ON DELETE CASCADE,
    CONSTRAINT check_item CHECK (
        (item_type = 'RESOURCE' AND resource_id IS NOT NULL AND session_id IS NULL) OR
        (item_type = 'SESSION' AND session_id IS NOT NULL AND resource_id IS NULL)
    ),
    CONSTRAINT unique_user_item UNIQUE (user_id, resource_id, session_id)
);

CREATE INDEX idx_cart_user ON cart_items(user_id);
CREATE INDEX idx_cart_resource ON cart_items(resource_id);
CREATE INDEX idx_cart_session ON cart_items(session_id);
CREATE INDEX idx_cart_item_type ON cart_items(item_type);
CREATE INDEX idx_saved_user ON saved_resources(user_id);
CREATE INDEX idx_saved_resource ON saved_resources(resource_id);
CREATE INDEX idx_saved_timestamp ON saved_resources(saved_at DESC);

-- Table pour tracker les utilisateurs qui ont vu une ressource
CREATE TABLE IF NOT EXISTS resource_views (
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, resource_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE
);

CREATE INDEX idx_views_user ON resource_views(user_id);
CREATE INDEX idx_views_resource ON resource_views(resource_id);
CREATE INDEX idx_views_timestamp ON resource_views(viewed_at DESC);

CREATE INDEX IF NOT EXISTS idx_resource_categories_resource ON resource_categories(resource_id);
CREATE INDEX IF NOT EXISTS idx_resource_categories_category ON resource_categories(category_id);

CREATE INDEX IF NOT EXISTS idx_resources_title ON resources USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_resources_content ON resources USING gin(to_tsvector('english', content));

-- Fonction pour mettre à jour le timestamp updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pour updated_at sur resources
DROP TRIGGER IF EXISTS update_resources_updated_at ON resources;
CREATE TRIGGER update_resources_updated_at 
    BEFORE UPDATE ON resources 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ===== TABLES POUR LE SYSTÈME DE TUTORAT =====

-- Profils des tuteurs
CREATE TABLE IF NOT EXISTS tutor_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE,
    bio TEXT,
    hourly_rate DECIMAL(10, 2) NOT NULL,
    subjects TEXT[], -- Array de matières enseignées
    availability TEXT, -- Exemple: "Mon-Fri, 2-6 PM"
    total_sessions INTEGER DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    is_premium BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tutor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_tutor_user ON tutor_profiles(user_id);
CREATE INDEX idx_tutor_rating ON tutor_profiles(average_rating DESC);
CREATE INDEX idx_tutor_rate ON tutor_profiles(hourly_rate);

-- Sessions de tutorat proposées
CREATE TABLE IF NOT EXISTS tutor_sessions (
    id SERIAL PRIMARY KEY,
    tutor_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INTEGER NOT NULL, -- Durée en minutes (ex: 60, 90, 120)
    max_students INTEGER DEFAULT 1, -- Nombre max d'étudiants par session
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_tutor FOREIGN KEY (tutor_id) REFERENCES tutor_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_tutor ON tutor_sessions(tutor_id);
CREATE INDEX idx_sessions_subject ON tutor_sessions(subject);
CREATE INDEX idx_sessions_price ON tutor_sessions(price);

-- Réservations de sessions
CREATE TABLE IF NOT EXISTS session_bookings (
    id SERIAL PRIMARY KEY,
    session_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    booking_date TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    payment_status VARCHAR(50) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED')),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_session FOREIGN KEY (session_id) REFERENCES tutor_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_bookings_session ON session_bookings(session_id);
CREATE INDEX idx_bookings_student ON session_bookings(student_id);
CREATE INDEX idx_bookings_status ON session_bookings(status);
CREATE INDEX idx_bookings_date ON session_bookings(booking_date);

-- Avis sur les tuteurs
CREATE TABLE IF NOT EXISTS tutor_reviews (
    id SERIAL PRIMARY KEY,
    tutor_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    booking_id INTEGER,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_tutor FOREIGN KEY (tutor_id) REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES session_bookings(id) ON DELETE SET NULL,
    UNIQUE (tutor_id, student_id, booking_id)
);

CREATE INDEX idx_reviews_tutor ON tutor_reviews(tutor_id);
CREATE INDEX idx_reviews_student ON tutor_reviews(student_id);
CREATE INDEX idx_reviews_rating ON tutor_reviews(rating);
CREATE INDEX idx_reviews_created ON tutor_reviews(created_at DESC);

-- Trigger pour mettre à jour updated_at sur tutor_profiles
DROP TRIGGER IF EXISTS update_tutor_profiles_updated_at ON tutor_profiles;
CREATE TRIGGER update_tutor_profiles_updated_at 
    BEFORE UPDATE ON tutor_profiles 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
