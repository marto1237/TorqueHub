CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       salt VARCHAR(255) NOT NULL,
                       username VARCHAR(255) NOT NULL,
                       UNIQUE (username)
);

CREATE TABLE questions (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           total_answers INT NOT NULL,
                           total_comments INT NOT NULL,
                           views INT NOT NULL,
                           votes INT NOT NULL,
                           asked_time DATETIME(6) NOT NULL,
                           best_answer_id BIGINT,
                           last_activity_time DATETIME(6) NOT NULL,
                           user_id BIGINT NOT NULL,
                           description LONGTEXT CHARACTER SET utf8mb4 NOT NULL,
                           title TEXT CHARACTER SET utf8mb4 NOT NULL,
                           FOREIGN KEY (user_id) REFERENCES users(id),
                           FOREIGN KEY (best_answer_id) REFERENCES answers(id)
);

CREATE TABLE answers (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         votes INT NOT NULL,
                         answered_time DATETIME(6) NOT NULL,
                         question_id BIGINT NOT NULL,
                         user_id BIGINT NOT NULL,
                         text LONGTEXT CHARACTER SET utf8mb4 NOT NULL,
                         edited BIT(1) NOT NULL,
                         FOREIGN KEY (question_id) REFERENCES questions(id),
                         FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE comments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          votes INT NOT NULL,
                          answer_id BIGINT NOT NULL,
                          commented_time DATETIME(6) NOT NULL,
                          user_id BIGINT NOT NULL,
                          text LONGTEXT CHARACTER SET utf8mb4 NOT NULL,
                          is_edited BIT(1) NOT NULL,
                          FOREIGN KEY (answer_id) REFERENCES answers(id),
                          FOREIGN KEY (user_id) REFERENCES users(id)
);
