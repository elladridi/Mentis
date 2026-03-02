-- ============================================
-- MENTIS Database - FINAL CLEAN VERSION
-- Mental Health Platform with Complete Data
-- ============================================

DROP DATABASE IF EXISTS mentis;
CREATE DATABASE mentis;
USE mentis;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- ============================================
-- TABLE: user (All users from both databases)
-- ============================================
CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    dateofbirth VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(1000) NOT NULL,
    face_data TEXT DEFAULT NULL,
    face_enabled TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT ALL USERS (Combined from both databases)
-- ============================================
INSERT INTO user (id, firstname, lastname, phone, dateofbirth, type, email, password, face_enabled) VALUES
-- Users from first database (with face_enabled field)
(1, 'test', 'tet', '26667352', '2003-12-23', 'Admin', 'test@gmail.com', '9af15b336e6a9619928537df30b2e6a2376569fcf9d7e773eccede65606529a0', 0),
(2, 'eya', 'mhadhbi', '99819349', '2003-06-23', 'Patient', 'eyamhadhbi@gmail.com', 'ee1ecd7240acd6f395028c21a8b6b82f00ef9841a3a0898893944ff95dcf0122', 0),
(3, 'ahmed', 'rhouma', '96022691', '2000-10-16', 'Patient', 'ahmedrhouma24@gmail.com', 'fd138c8a2c2cd612e0564cd03db7eff474b49a3f0154b7e6c8e419d101db11fb', 0),
(4, 'ela', 'dridi', '95126352', '2003-05-26', 'Patient', 'elladridi96@gmail.com', 'db3c234b8188d8df179b5ca969976d1e2f925bcf092f6e7a1f97e6750aa52a1a', 0),
(5, 'skander', 'chamkhi', '99819349', '2000-12-23', 'Patient', 'skanderchamkhi@gmail.com', '7e071fd9b023ed8f18458a73613a0834f6220bd5cc50357ba3493c6040a9ea8c', 0),
(6, 'test', 'tt', '24571231', '2000-06-23', 'psychologist', 'testtt@gmail.com', 'f348d5628621f3d8f59c8cabda0f8eb0aa7e0514a90be7571020b1336f26c113', 0),
(7, 'sytej', 'bouhlila', '27033553', '1999-06-23', 'Patient', 'sytejbouhlila@gmail.com', '9af15b336e6a9619928537df30b2e6a2376569fcf9d7e773eccede65606529a0', 1),
(8, 'aaa', 'bb', '26485352', '2003-12-20', 'Admin', 'admin@gmail.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 0),
(9, 'Arij', 'Bouhlila', '26667352', '2003-12-23', 'Psychologist', 'arijbouhlila9@gmail.com', '49efec89531d74840168da3cb78afc1fe2029351f3c86e891ec1312fc1342f63', 0),

-- Users from second database (will have face_enabled=0 by default)
(10, 'ella', 'dridi', '55595801', '26-05-2003', 'Patient', 'ella.dridi@esprit.tn', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 0),
(11, 'arij', 'bouhlila', '155878787', '28-12-2003', 'Admin', 'arij@esprit.tn', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 0),
(12, 'wee', 'wee', '545484854', '26-04-2003', 'Psychologist', 'we@esprit.tn', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 0),
(13, 'ahmed', 'zekri', '20666202', '23-01-2004', 'Patient', 'ahmedzekri20666202@gmail.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 0),
(14, 'zikou', '.', '20666202', '23-01-2004', 'Psychologist', 'ahmedzekri@gmail.com', '2ac9a6746aca543af8dff39894cfe8173afba21eb01c6fae33d52947222855ef', 0),
(15, 'omar', 'omar', '232222222', '2003-11-11', 'Admin', 'TEST@TEST.COM', '984751f9767837e64aaa45b53bc1cfe950a7e494971600edd60c8cf0edb22796', 0),
(16, 'AZER', 'AZER', '343242341', '234234234', 'Patient', 'AZER@AZER.COM', '98dc384177d6e033313c5f85fe1320e2e61e28d2ff555cef0bbee7ef65a82d05', 0),
(17, 'AZER', 'AZER', '23412344', '2003-11-11', 'Patient', 'TEST@TEST.TEST', '984751f9767837e64aaa45b53bc1cfe950a7e494971600edd60c8cf0edb22796', 0),
(18, 'AAA', 'AAA', '93222222', '2003-11-11', 'Psychologist', 'PSI@PSI.PSI', 'cb6c5ac68a624a693954824deee9b55280446889049daa16cfc5d7d6653f1da1', 0),
(19, 'OMAR', 'KAMMOUN', '93322211', '2003-11-11', 'Patient', 'AAAA@AAAA.AAA', 'cb1ad2119d8fafb69566510ee712661f9f14b83385006ef92aec47f523a38358', 0);

-- ============================================
-- TABLE: user_old (backup/archive)
-- ============================================
CREATE TABLE user_old (
    id INT PRIMARY KEY AUTO_INCREMENT,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    dateofbirth VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(1000) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO user_old
-- ============================================
INSERT INTO user_old (id, firstname, lastname, phone, dateofbirth, type, email, password) VALUES
(7, 'ella', 'dridi', '55595801', '26-05-2003', 'Patient', 'ella.dridi@esprit.tn', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e'),
(8, 'arij', 'bouhlila', '155878787', '28-12-2003', 'Admin', 'arij@esprit.tn', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3'),
(9, 'wee', 'wee', '545484854', '26-04-2003', 'Psychologist', 'we@esprit.tn', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3'),
(10, 'ahmed', 'zekri', '20666202', '23-01-2004', 'Patient', 'ahmedzekri20666202@gmail.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3'),
(11, 'zikou', '.', '20666202', '23-01-2004', 'Psychologist', 'ahmedzekri@gmail.com', '2ac9a6746aca543af8dff39894cfe8173afba21eb01c6fae33d52947222855ef'),
(12, 'omar', 'omar', '232222222', '2003-11-11', 'Admin', 'TEST@TEST.COM', '984751f9767837e64aaa45b53bc1cfe950a7e494971600edd60c8cf0edb22796'),
(13, 'AZER', 'AZER', '343242341', '234234234', 'Patient', 'AZER@AZER.COM', '98dc384177d6e033313c5f85fe1320e2e61e28d2ff555cef0bbee7ef65a82d05'),
(14, 'AZER', 'AZER', '23412344', '2003-11-11', 'Patient', 'TEST@TEST.TEST', '984751f9767837e64aaa45b53bc1cfe950a7e494971600edd60c8cf0edb22796'),
(15, 'AAA', 'AAA', '93222222', '2003-11-11', 'Psychologist', 'PSI@PSI.PSI', 'cb6c5ac68a624a693954824deee9b55280446889049daa16cfc5d7d6653f1da1'),
(16, 'OMAR', 'KAMMOUN', '93322211', '2003-11-11', 'Patient', 'AAAA@AAAA.AAA', 'cb1ad2119d8fafb69566510ee712661f9f14b83385006ef92aec47f523a38358');

-- ============================================
-- TABLE: assessment
-- ============================================
CREATE TABLE assessment (
    assessment_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    type VARCHAR(100) DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at DATE DEFAULT CURDATE(),
    image_path VARCHAR(500) DEFAULT NULL,
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO assessment
-- ============================================
INSERT INTO assessment (assessment_id, title, description, type, status, image_path) VALUES
(11, 'DEPRESSION', 'Measures symptoms of depression including persistent sadness, loss of interest, and changes in sleep or appetite. Helps identify if professional help might be beneficial.', 'Depression', 'Active', 'assessment_images/assessment_1770405711552.jpg'),
(12, 'Anxiety', 'Evaluates feelings of anxiety, nervousness, and excessive worry that interfere with daily activities. Identifies anxiety patterns and severity.', 'Anxiety', 'Active', 'assessment_images/assessment_1770405805779.jpg'),
(13, 'Well-being Assessment', 'Measures overall life satisfaction, happiness, and positive functioning. Assesses emotional health and quality of life indicators.', 'Wellness', 'Active', 'assessment_images/assessment_1770405902229.jpg'),
(14, 'Stress', 'Evaluates stress levels and coping mechanisms. Identifies stress triggers and provides recommendations for stress management techniques.', 'Stress', 'Active', 'assessment_images/assessment_1770405929962.jpg'),
(15, 'General Mental Health Assessment', 'Comprehensive screening covering multiple aspects of mental health including mood, anxiety, sleep, and daily functioning.', 'General', 'Active', 'assessment_images/assessment_1770405958762.jpg'),
(16, 'Customized Assessment', 'Create your own assessment with personalized questions and categories. Ideal for tracking specific concerns or goals over time.', 'Custom', 'Inactive', 'assessment_images/assessment_1770405989351.jpg'),
(18, 'AZREAZR', 'AZERZER', 'Depression', 'Active', 'assessment_images/assessment_1770957026300.png');

-- ============================================
-- TABLE: question
-- ============================================
CREATE TABLE question (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    assessment_id INT DEFAULT NULL,
    text TEXT NOT NULL,
    scale VARCHAR(50) DEFAULT NULL,
    FOREIGN KEY (assessment_id) REFERENCES assessment(assessment_id) ON DELETE CASCADE,
    INDEX idx_assessment (assessment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO question
-- ============================================
INSERT INTO question (question_id, assessment_id, text, scale) VALUES
(7, 11, 'Over the past two weeks, how often have you felt down, depressed, or hopeless?', 'Never/Rarely/Sometimes/often/Always'),
(8, 11, 'How often have you had little interest or pleasure in doing things you usually enjoy?', 'Never/Rarely/Sometimes/often/Always'),
(10, 11, 'Have you experienced changes in your sleep patterns or appetite recently?', 'Never/Rarely/Sometimes/often/Always'),
(11, 12, 'How often do you feel nervous, anxious, or on edge?', 'Never/Rarely/Sometimes/often/Always'),
(12, 12, 'Do you find it difficult to stop or control worrying?', 'Never/Rarely/Sometimes/often/Always'),
(13, 12, 'On rate (1-5) how much do your worries interfere with your daily activities?', '1-5'),
(14, 13, 'Overall, how satisfied are you with your life nowadays?', 'Never/Rarely/Sometimes/often/Always'),
(15, 13, 'How often do you feel positive and optimistic about your future?', 'Never/Rarely/Sometimes/often/Always'),
(16, 13, 'To what extent do you feel the things you do in your life are worthwhile?', 'Never/Rarely/Sometimes/often/Always'),
(17, 14, 'How often do you feel stressed or overwhelmed by your daily responsibilities?', 'Never/Rarely/Sometimes/often/Always'),
(18, 14, 'On rate of 1 to 5 how well are you able to cope with stressful situations?', '1-5'),
(19, 14, 'Do you experience physical symptoms (headaches, fatigue, etc.) when stressed?', 'Never/Rarely/Sometimes/often/Always'),
(20, 15, 'How would you rate your overall mental health over the past month?', '1-5'),
(21, 15, 'How often do your emotional issues interfere with your social life or relationships?', 'Never/Rarely/Sometimes/often/Always'),
(22, 15, 'Have you felt the need for professional help or support for your mental health?', 'No/Yes');

-- ============================================
-- TABLE: assessmentresult
-- ============================================
CREATE TABLE assessmentresult (
    result_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT DEFAULT NULL,
    assessment_id INT DEFAULT NULL,
    total_score INT DEFAULT NULL,
    risk_level VARCHAR(20) DEFAULT NULL,
    interpretation TEXT DEFAULT NULL,
    recommended_content TEXT DEFAULT NULL,
    suggest_session TINYINT(1) DEFAULT NULL,
    taken_at DATE DEFAULT CURDATE(),
    FOREIGN KEY (assessment_id) REFERENCES assessment(assessment_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_assessment (assessment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO assessmentresult
-- ============================================
INSERT INTO assessmentresult (result_id, user_id, assessment_id, total_score, risk_level, interpretation, recommended_content, suggest_session, taken_at) VALUES
(15, 5, 11, 10, 'Low', 'Your scores indicate minimal concerns in this area. Continue with healthy habits.', '• Continue with healthy habits\n• Mindfulness practices\n• Regular exercise routine', 0, '2026-02-06'),
(16, 7, 11, 6, 'Low', 'Your scores indicate minimal concerns in this area. Continue with healthy habits.', '• Continue with healthy habits\n• Mindfulness practices\n• Regular exercise routine', 0, '2026-02-06'),
(17, 1, 11, 10, 'Low', 'Your scores indicate minimal concerns in this area. Continue with healthy habits.', '• Continue with healthy habits\n• Mindfulness practices\n• Regular exercise routine', 0, '2026-02-06'),
(18, 3, 11, 7, 'Low', 'Your scores indicate minimal concerns in this area. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Continue with healthy habits\n• Mindfulness practices for maintenance\n• Regular exercise routine\n', 0, '2026-02-06'),
(19, 9, 11, 9, 'Moderate', 'Your scores suggest some areas that may need attention. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Stress management techniques\n• Self-help resources and books\n• Consider talking to a counselor\n• Sleep hygiene improvement strategies\n', 0, '2026-02-06'),
(20, 1, 14, 10, 'High', 'Your scores indicate significant concerns that should be addressed. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Professional consultation recommended\n• Support groups available\n• Crisis hotline: 1-800-273-8255\n• Consider stress management workshop\n', 1, '2026-02-06'),
(21, 7, 11, 6, 'Mild', 'Your scores suggest some areas that may need attention. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Stress management techniques\n• Self-help resources and books\n• Consider talking to a counselor\n• Sleep hygiene improvement strategies\n', 0, '2026-02-09'),
(22, 7, 11, 6, 'Mild', 'Your scores suggest some areas that may need attention. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Stress management techniques\n• Self-help resources and books\n• Consider talking to a counselor\n• Sleep hygiene improvement strategies\n', 0, '2026-02-09'),
(23, 7, 11, 6, 'Mild', 'Your scores suggest some areas that may need attention. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Stress management techniques\n• Self-help resources and books\n• Consider talking to a counselor\n• Sleep hygiene improvement strategies\n', 0, '2026-02-09'),
(24, 7, 11, 7, 'Moderate', 'Your scores suggest some areas that may need attention. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Stress management techniques\n• Self-help resources and books\n• Consider talking to a counselor\n• Sleep hygiene improvement strategies\n', 0, '2026-02-10'),
(25, 10, 11, 4, 'Mild', 'Your scores suggest some areas that may need attention. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Stress management techniques\n• Self-help resources and books\n• Consider talking to a counselor\n• Sleep hygiene improvement strategies\n', 0, '2026-02-12'),
(26, 16, 11, 3, 'Low', 'Your scores indicate minimal concerns in this area. The AI analysis provides personalized insights below.', 'Based on your assessment results:\n• Continue with healthy habits\n• Mindfulness practices for maintenance\n• Regular exercise routine\n', 0, '2026-02-13');

-- ============================================
-- TABLE: sessions
-- ============================================
CREATE TABLE sessions (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    session_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'Scheduled',
    reserved_by INT DEFAULT NULL,
    reserved_at DATETIME DEFAULT NULL,
    category VARCHAR(100) DEFAULT 'General',
    popularity INT DEFAULT 0,
    average_rating DOUBLE DEFAULT 0,
    max_participants INT DEFAULT 20,
    current_participants INT DEFAULT 0,
    price DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (reserved_by) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_session_date (session_date),
    INDEX idx_session_type (session_type),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ============================================
-- INSERT INTO sessions
-- ============================================
INSERT INTO sessions (session_id, title, session_date, start_time, end_time, location, session_type, status, reserved_by, category, max_participants, price) VALUES
(1, 'teststest', '2026-02-13', '09:00:00', '10:00:00', 'mourouj', 'Family', 'active', NULL, 'General', 15, 0),
(2, 'test2.0', '2026-02-13', '09:00:00', '10:00:00', 'at home', 'Online', 'active', NULL, 'General', 20, 0),
(3, 'Completed Group Session', '2026-02-10', '10:00:00', '11:30:00', 'Room 205', 'Group', 'active', NULL, 'General', 20, 25),
(4, 'Past Individual Session', '2026-02-06', '15:00:00', '16:00:00', 'Online', 'Individual', 'active', NULL, 'General', 1, 50),
(5, 'Morning Therapy', '2026-02-14', '09:00:00', '10:00:00', 'Room 101', 'Individual', 'active', NULL, 'General', 1, 45),
(6, 'Group Session', '2026-02-15', '14:00:00', '15:30:00', 'Room 205', 'Group', 'active', NULL, 'General', 20, 30),
(7, 'Evening Relaxation', '2026-02-16', '18:00:00', '19:00:00', 'Online', 'Online', 'active', NULL, 'General', 30, 15),
(8, 'Family Therapy', '2026-02-17', '10:00:00', '11:30:00', 'Room 102', 'Family', 'active', NULL, 'General', 8, 60),
(9, 'Couple Counseling', '2026-02-18', '15:00:00', '16:00:00', 'Office 3', 'Couple', 'active', NULL, 'General', 2, 55),
(10, 'Mindfulness Session', '2026-02-19', '11:00:00', '12:00:00', 'Garden Room', 'Individual', 'active', 7, 'General', 1, 40),
(11, 'Past Group Session', '2026-02-08', '10:00:00', '11:30:00', 'Room 205', 'Group', 'active', 7, 'General', 20, 25),
(12, 'therapy', '2026-02-20', '09:00:00', '10:00:00', 'esprit', 'Group', 'scheduled', NULL, 'General', 15, 0);

-- ============================================
-- TABLE: session_review
-- ============================================
CREATE TABLE session_review (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT NOT NULL,
    patient_id INT NOT NULL,
    rating INT(1) NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT DEFAULT NULL,
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_session (session_id),
    INDEX idx_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ============================================
-- INSERT INTO session_review
-- ============================================
INSERT INTO session_review (session_id, patient_id, rating, comment) VALUES
(8, 7, 2, 'i liked the vibe'),
(7, 7, 2, 'i waited for too long and the doctor seemed off');

-- ============================================
-- TABLE: content_node
-- ============================================
CREATE TABLE content_node (
    node_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    pdf_path VARCHAR(500) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by INT NOT NULL,
    parent_node_id INT DEFAULT NULL,
    assigned_users TEXT DEFAULT '[]',
    FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_node_id) REFERENCES content_node(node_id) ON DELETE SET NULL,
    INDEX idx_created_by (created_by),
    INDEX idx_parent (parent_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO content_node
-- ============================================
INSERT INTO content_node (node_id, title, description, pdf_path, created_at, created_by, parent_node_id, assigned_users) VALUES
(24, 'AERZAER', 'AKAAKA\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"', 'uploads\\content\\0_20260213_044633_Tutorial 1.pdf', '2026-02-13 04:46:33', 6, NULL, '[10]'),
(28, 'YOU ARE NOT YOUR PAST', 'HOW BRAIN PLASTISITY CAN SHAPE YOUR CARER', NULL, '2026-02-13 13:35:57', 9, NULL, '[10]'),
(29, 'FFFFFFFFFFFFFFFFFFFFFFFFFFFFF', 'FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF', 'uploads\\content\\0_20260213_133739_Lab1- DDL.pdf', '2026-02-13 13:37:39', 9, NULL, '[10]'),
(30, 'TEST', 'TESEEEEEEEEEEEEEEEEEEEEEEEEEETETET', NULL, '2026-02-17 11:11:26', 6, NULL, '[7,10]');

-- ============================================
-- TABLE: content_path
-- ============================================
CREATE TABLE content_path (
    path_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    node_id INT NOT NULL,
    accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (node_id) REFERENCES content_node(node_id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_node (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO content_path
-- ============================================
INSERT INTO content_path (path_id, user_id, node_id, accessed_at) VALUES
(18, 10, 29, '2026-02-17 11:15:36');

-- ============================================
-- TABLE: goal
-- ============================================
CREATE TABLE goal (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    deadline DATE DEFAULT NULL,
    progress INT DEFAULT NULL,
    status VARCHAR(50) DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO goal
-- ============================================
INSERT INTO goal (id, user_id, description, deadline, progress, status) VALUES
(49, 1, 'yesin', '2026-02-04', 49, 'En cours'),
(53, 2, 'hedi', '2026-01-08', 75, 'En attente');

-- ============================================
-- TABLE: mood
-- ============================================
CREATE TABLE mood (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT DEFAULT NULL,
    feeling VARCHAR(50) DEFAULT NULL,
    note TEXT DEFAULT NULL,
    date DATETIME DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO mood
-- ============================================
INSERT INTO mood (id, user_id, feeling, note, date) VALUES
(28, 1, ' 😐 Neutre 😊 Heureux', '12', '2026-02-12 19:27:44'),
(29, 1, ' 😔 Triste', 'aaaaaaaaa', '2026-02-12 19:27:36');

-- ============================================
-- TABLE: events
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
    created_by INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_event_type (event_type),
    INDEX idx_date_time (date_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO events
-- ============================================
INSERT INTO events (id, title, description, date_time, location, max_participants, current_participants, event_type, price, status, created_by) VALUES
(1, 'Mindfulness Workshop', 'Learn mindfulness techniques for daily stress reduction and mental clarity. Covers breathing exercises, body scanning, and mindful meditation.', DATE_ADD(NOW(), INTERVAL 7 DAY), 'Online (Zoom)', 30, 5, 'WORKSHOP', 25.00, 'UPCOMING', 8),
(2, 'Anxiety Support Group', 'Weekly support group for anxiety disorders. Share experiences and learn coping strategies in a safe environment.', DATE_ADD(NOW(), INTERVAL 2 DAY), 'Wellness Center Room 101', 15, 8, 'GROUP_THERAPY', 0.00, 'UPCOMING', 8),
(3, 'Stress Management Seminar', 'Expert-led seminar on stress management including time management, relaxation techniques, and cognitive restructuring.', DATE_ADD(NOW(), INTERVAL 10 DAY), 'Community Hall A', 50, 12, 'SEMINAR', 15.00, 'UPCOMING', 8),
(4, 'Art Therapy Social', 'Express yourself through art in a supportive group setting. No artistic experience required.', DATE_ADD(NOW(), INTERVAL 14 DAY), 'Art Studio B', 20, 3, 'SOCIAL', 10.00, 'UPCOMING', 8),
(5, 'Meditation Basics', 'Introduction to meditation for complete beginners. Learn techniques you can practice at home every day.', DATE_ADD(NOW(), INTERVAL 5 DAY), 'Online (Zoom)', 40, 15, 'WORKSHOP', 0.00, 'UPCOMING', 8);

-- ============================================
-- TABLE: event_registrations
-- ============================================
CREATE TABLE event_registrations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT DEFAULT NULL,
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
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL,
    UNIQUE KEY unique_email_event (email, event_id),
    INDEX idx_event_id (event_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_registration_date (registration_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================
-- INSERT INTO event_registrations (ALL user_ids exist in user table)
-- ============================================
INSERT INTO event_registrations (id, event_id, user_id, user_name, email, phone, ticket_type, number_of_tickets, total_price, status, payment_method, special_requests) VALUES
(1, 1, 1, 'John Doe', 'john@email.com', '+1234567890', 'VIP', 1, 37.50, 'CONFIRMED', 'CREDIT_CARD', 'Need wheelchair access'),
(2, 1, 2, 'Jane Smith', 'jane@email.com', '+1234567891', 'STANDARD', 2, 50.00, 'CONFIRMED', 'PAYPAL', NULL),
(3, 1, 3, 'Alice Brown', 'alice@email.com', '+1234567892', 'STANDARD', 1, 25.00, 'PENDING', 'CREDIT_CARD', NULL),
(4, 2, 1, 'John Doe', 'john@email.com', '+1234567890', 'STANDARD', 1, 0.00, 'CONFIRMED', 'FREE', NULL),
(5, 2, 5, 'Bob Wilson', 'bob@email.com', '+1234567893', 'STANDARD', 1, 0.00, 'CONFIRMED', 'FREE', 'First time attending'),
(6, 2, 4, 'Carol Davis', 'carol@email.com', '+1234567894', 'STANDARD', 1, 0.00, 'CANCELLED', 'FREE', NULL);

-- ============================================
-- Reset AUTO_INCREMENT values
-- ============================================
ALTER TABLE user AUTO_INCREMENT = 20;
ALTER TABLE user_old AUTO_INCREMENT = 17;
ALTER TABLE assessment AUTO_INCREMENT = 19;
ALTER TABLE question AUTO_INCREMENT = 24;
ALTER TABLE assessmentresult AUTO_INCREMENT = 27;
ALTER TABLE sessions AUTO_INCREMENT = 13;
ALTER TABLE session_review AUTO_INCREMENT = 4;
ALTER TABLE content_node AUTO_INCREMENT = 34;
ALTER TABLE content_path AUTO_INCREMENT = 20;
ALTER TABLE goal AUTO_INCREMENT = 54;
ALTER TABLE mood AUTO_INCREMENT = 30;
ALTER TABLE events AUTO_INCREMENT = 6;
ALTER TABLE event_registrations AUTO_INCREMENT = 7;

-- ============================================
-- VERIFICATION QUERIES (Optional - can be removed)
-- ============================================
-- SELECT 'USER COUNT' as info, COUNT(*) as value FROM user
-- UNION ALL
-- SELECT 'USER TYPES', COUNT(DISTINCT type) FROM user
-- UNION ALL
-- SELECT 'EVENT REGISTRATIONS', COUNT(*) FROM event_registrations
-- UNION ALL
-- SELECT 'VALID USER LINKS', COUNT(*) FROM event_registrations WHERE user_id IS NOT NULL;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;