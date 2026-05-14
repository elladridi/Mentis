
-- ============================================
-- MENTIS Database Setup
-- Mental Health Event Management Platform
-- ============================================

CREATE DATABASE IF NOT EXISTS mentalhealth_db;
USE mentalhealth_db;

-- Drop existing tables
DROP TABLE IF EXISTS event_registrations;
DROP TABLE IF EXISTS events;

-- ============================================
-- TABLE 1: EVENTS
-- ============================================
CREATE TABLE events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    date_time DATETIME NOT NULL,
    location VARCHAR(255),
    max_participants INT NOT NULL,
    current_participants INT DEFAULT 0,
    event_type VARCHAR(50),
    price DECIMAL(10,2) DEFAULT 0.00,
    image_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'UPCOMING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_event_type (event_type),
    INDEX idx_date_time (date_time),
    INDEX idx_status (status)
);

-- ============================================
-- TABLE 2: EVENT REGISTRATIONS
-- ============================================
CREATE TABLE event_registrations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    ticket_type VARCHAR(50) DEFAULT 'STANDARD',
    number_of_tickets INT DEFAULT 1,
    total_price DECIMAL(10,2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    payment_method VARCHAR(50),
    special_requests TEXT,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY unique_email_event (email, event_id),
    INDEX idx_event_id (event_id),
    INDEX idx_status (status),
    INDEX idx_registration_date (registration_date)
);

-- ============================================
-- SAMPLE DATA: EVENTS
-- ============================================
INSERT INTO events (title, description, date_time, location, max_participants,
    current_participants, event_type, price, status) VALUES
('Mindfulness Workshop',
 'Learn mindfulness techniques for daily stress reduction and mental clarity. Covers breathing exercises, body scanning, and mindful meditation.',
 DATE_ADD(NOW(), INTERVAL 7 DAY), 'Online (Zoom)', 30, 5, 'WORKSHOP', 25.00, 'UPCOMING'),

('Anxiety Support Group',
 'Weekly support group for anxiety disorders. Share experiences and learn coping strategies in a safe environment.',
 DATE_ADD(NOW(), INTERVAL 2 DAY), 'Wellness Center Room 101', 15, 8, 'GROUP_THERAPY', 0.00, 'UPCOMING'),

('Stress Management Seminar',
 'Expert-led seminar on stress management including time management, relaxation techniques, and cognitive restructuring.',
 DATE_ADD(NOW(), INTERVAL 10 DAY), 'Community Hall A', 50, 12, 'SEMINAR', 15.00, 'UPCOMING'),

('Art Therapy Social',
 'Express yourself through art in a supportive group setting. No artistic experience required.',
 DATE_ADD(NOW(), INTERVAL 14 DAY), 'Art Studio B', 20, 3, 'SOCIAL', 10.00, 'UPCOMING'),

('Meditation Basics',
 'Introduction to meditation for complete beginners. Learn techniques you can practice at home every day.',
 DATE_ADD(NOW(), INTERVAL 5 DAY), 'Online (Zoom)', 40, 15, 'WORKSHOP', 0.00, 'UPCOMING');

-- ============================================
-- SAMPLE DATA: REGISTRATIONS
-- ============================================
INSERT INTO event_registrations (event_id, user_name, email, phone, ticket_type,
    number_of_tickets, total_price, status, payment_method, special_requests) VALUES
(1, 'John Doe', 'john@email.com', '+1234567890', 'VIP', 1, 37.50, 'CONFIRMED', 'CREDIT_CARD', 'Need wheelchair access'),
(1, 'Jane Smith', 'jane@email.com', '+1234567891', 'STANDARD', 2, 50.00, 'CONFIRMED', 'PAYPAL', NULL),
(1, 'Alice Brown', 'alice@email.com', '+1234567892', 'STANDARD', 1, 25.00, 'PENDING', 'CREDIT_CARD', NULL),
(2, 'John Doe', 'john@email.com', '+1234567890', 'STANDARD', 1, 0.00, 'CONFIRMED', 'FREE', NULL),
(2, 'Bob Wilson', 'bob@email.com', '+1234567893', 'STANDARD', 1, 0.00, 'CONFIRMED', 'FREE', 'First time attending'),
(2, 'Carol Davis', 'carol@email.com', '+1234567894', 'STANDARD', 1, 0.00, 'CANCELLED', 'FREE', NULL),
(3, 'Jane Smith', 'jane@email.com', '+1234567891', 'VIP', 1, 22.50, 'CONFIRMED', 'CREDIT_CARD', NULL),
(3, 'David Lee', 'david@email.com', '+1234567895', 'STANDARD', 3, 45.00, 'CONFIRMED', 'PAYPAL', 'Bringing 2 friends');