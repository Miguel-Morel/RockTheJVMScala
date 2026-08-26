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
      'rockthejvm',
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
    'ricardorulez',
    'ricardo',
    'cardin',
    'rock the jvm',
    'RECRUITER'
);