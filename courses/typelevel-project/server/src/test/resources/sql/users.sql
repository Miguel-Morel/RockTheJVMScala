CREATE TABLE users (
    email text NOT NULL,
    hashedPassword text NOT NULL,
    firstName text,
    lastName text,
    company text,
    role text NOT NULL
);

ALTER TABLE users
ADD CONSTRAINT pk_users PRIMARY KEY (email);

INSERT INTO users (
    email,
    hashedPassword,
    firstName,
    lastName,
    company,
    role
) VALUES (
    'daniel@rockthejvm.com',
      '$2a$10$32S4CnVVXV8oANEWcxby0.KRCrmaIwfkzVLMlS8dyD/Y7VFkQuHzG',
      'daniel',
      'ciocirlan',
      'rock the jvm',
      'ADMIN'
);

INSERT INTO users (
    email,
    hashedPassword,
    firstName,
    lastName,
    company,
    role
) VALUES (
    'ricardo@rockthejvm.com',
    '$2a$10$W88WykuhIfAuA3JBXhG.KOf0DR8yqUUOWmPnd7pdQWic/7AK6f8lW',
    'ricardo',
    'cardin',
    'rock the jvm',
    'RECRUITER'
);