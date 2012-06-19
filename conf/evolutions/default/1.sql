# Database model
 
# --- !Ups

CREATE SEQUENCE user_id_seq;
CREATE TABLE users (
    id integer NOT NULL DEFAULT nextval('user_id_seq'),
    name varchar(255),
    email varchar(255) NOT NULL,
    github_username varchar(255),
    open_id varchar(255),
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE role_id_seq;
CREATE TABLE roles (
    id integer NOT NULL DEFAULT nextval('role_id_seq'),
    name varchar(255),
    description text,
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE user_role_id_seq;
CREATE TABLE user_roles (
    id integer NOT NULL DEFAULT nextval('user_role_id_seq'),
    
    user_id integer NOT NULL,
    role_id integer NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE location_id_seq;
CREATE TABLE locations (
    id integer NOT NULL DEFAULT nextval('location_id_seq'),
    country varchar(255),
    city varchar(255),
    postal_code varchar(255),
    full_address text,
    name varchar(255),
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE hackathons_id_seq;
CREATE TABLE hackathons (
    id integer NOT NULL DEFAULT nextval('hackathons_id_seq'),
    date timestamp,
    subject varchar(255),
    status varchar(255),
    
    submitter_id integer NOT NULL,
    location_id integer NOT NULL,
        
    FOREIGN KEY (submitter_id) REFERENCES users(id),
    FOREIGN KEY (location_id) REFERENCES locations(id),
    PRIMARY KEY (id)
);


CREATE SEQUENCE problem_id_seq;
CREATE TABLE problems (
    id integer NOT NULL DEFAULT nextval('problem_id_seq'),
    name varchar(255),
    description text,
    
    hackathon_id integer NOT NULL,
    submitter_id integer NOT NULL,
    
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    FOREIGN KEY (submitter_id) REFERENCES users(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE team_id_seq;
CREATE TABLE teams (
    id integer NOT NULL DEFAULT nextval('team_id_seq'),
    name varchar(255),
    
    creator_id integer NOT NULL,
    hackathon_id integer NOT NULL,
    problem_id integer NOT NULL,
    
    FOREIGN KEY (creator_id) REFERENCES users(id),
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    FOREIGN KEY (problem_id) REFERENCES problems(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE news_id_seq;
CREATE TABLE news (
    id integer NOT NULL DEFAULT nextval('news_id_seq'),
    title varchar(255),
    text text,
    labels text,
    published timestamp,
    
    author_id integer NOT NULL,
    
    FOREIGN KEY (author_id) REFERENCES users(id),
    PRIMARY KEY (id)
);
 
# --- !Downs

DROP TABLE hackathons;
DROP SEQUENCE hackathons_id_seq;

DROP TABLE locations;
DROP SEQUENCE location_id_seq;

DROP TABLE problems;
DROP SEQUENCE problem_id_seq;

DROP TABLE teams;
DROP SEQUENCE team_id_seq;

DROP TABLE users;
DROP SEQUENCE user_id_seq;

DROP TABLE user_roles;
DROP SEQUENCE user_role_id_seq;

DROP TABLE roles;
DROP SEQUENCE role_id_seq;

DROP TABLE news;
DROP SEQUENCE news_id_seq;
