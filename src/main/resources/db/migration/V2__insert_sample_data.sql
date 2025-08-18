-- Flyway migration: insert sample users and notes

INSERT INTO users (username, email, password, first_name, last_name, role, is_enabled, created_at, updated_at) VALUES
('admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin', 'User', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('john_doe', 'john.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'John', 'Doe', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jane_smith', 'jane.smith@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Jane', 'Smith', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO notes (title, content, priority, status, category, user_id, created_at, updated_at) VALUES
('Welcome to Notes App', 'This is your first note! You can create, read, update, and delete notes using this application.', 'MEDIUM', 'ACTIVE', 'Getting Started', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Project Planning', 'Plan the new project architecture and define requirements. Need to discuss with team members.', 'HIGH', 'ACTIVE', 'Work', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Shopping List', 'Buy groceries: milk, bread, eggs, fruits, vegetables. Don''t forget to check the expiry dates.', 'LOW', 'ACTIVE', 'Personal', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Meeting Notes', 'Team meeting scheduled for tomorrow at 10 AM. Agenda: project status, next milestones, resource allocation.', 'HIGH', 'ACTIVE', 'Work', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Book Recommendation', 'Read "Clean Code" by Robert C. Martin. Great book for software developers to improve coding practices.', 'MEDIUM', 'COMPLETED', 'Learning', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Workout Routine', 'Monday: Chest and triceps, Tuesday: Back and biceps, Wednesday: Legs, Thursday: Shoulders, Friday: Cardio', 'MEDIUM', 'ACTIVE', 'Health', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vacation Planning', 'Plan summer vacation to Bali. Book flights, hotels, and create itinerary. Budget: $2000', 'LOW', 'ACTIVE', 'Travel', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bug Fix', 'Fix the authentication bug in the login module. User sessions are not being maintained properly.', 'URGENT', 'ACTIVE', 'Work', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Learning Goals', 'Learn Spring Boot advanced features, Docker containerization, and microservices architecture.', 'MEDIUM', 'ACTIVE', 'Learning', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Archived Note', 'This is an old note that has been archived. It contains historical information.', 'LOW', 'ARCHIVED', 'Archive', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
