# Database model
 
# --- !Ups

CREATE SEQUENCE user_id_seq;
CREATE TABLE users (
    id integer NOT NULL DEFAULT nextval('user_id_seq'),
    name varchar(255),
    email varchar(255) NOT NULL,
	avatar_url text,    
    open_id varchar(255),
    github_username varchar(255),    
	twitter_account text,
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE role_id_seq;
CREATE TABLE roles (
    id integer NOT NULL DEFAULT nextval('role_id_seq'),
    name varchar(255),
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE user_role_id_seq;
CREATE TABLE users_roles (
    id integer NOT NULL DEFAULT nextval('user_role_id_seq'),
    
    user_id integer NOT NULL,
    role_id integer NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    UNIQUE (user_id, role_id),
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
	latitude float8,
	longitude float8,
	    
    PRIMARY KEY (id)
);

CREATE SEQUENCE hackathons_id_seq;
CREATE TABLE hackathons (
    id integer NOT NULL DEFAULT nextval('hackathons_id_seq'),
    date timestamp,
    subject varchar(255),
    status integer NOT NULL,
    
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
    problem_id integer,
    
    FOREIGN KEY (creator_id) REFERENCES users(id),
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    FOREIGN KEY (problem_id) REFERENCES problems(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE user_team_id_seq;
CREATE TABLE users_teams (
    id integer NOT NULL DEFAULT nextval('user_team_id_seq'),
    
    user_id integer NOT NULL,
    team_id integer NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (team_id) REFERENCES teams(id),
    UNIQUE (user_id, team_id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE news_id_seq;
CREATE TABLE news (
    id integer NOT NULL DEFAULT nextval('news_id_seq'),
    title varchar(255),
    text text,
    published_date timestamp,
    
    author_id integer NOT NULL,
    hackathon_id integer,
    
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE label_id_seq;
CREATE TABLE labels (
    id integer NOT NULL DEFAULT nextval('label_id_seq'),
    value varchar(255),
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE news_label_id_seq;
CREATE TABLE news_labels (
    id integer NOT NULL DEFAULT nextval('news_label_id_seq'),
    
    news_id integer NOT NULL,
    label_id integer NOT NULL,
    
    FOREIGN KEY (news_id) REFERENCES news(id),
    FOREIGN KEY (label_id) REFERENCES labels(id),
    UNIQUE (news_id, label_id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE prize_id_seq;
CREATE TABLE prizes (
    id integer NOT NULL DEFAULT nextval('prize_id_seq'),
    name varchar(255),
    description text,
    prize_order integer,
    founder_name varchar(255),
    founder_web_page varchar(255),
    
    hackathon_id integer NOT NULL,
    
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE sponsor_id_seq;
CREATE TABLE sponsors (
    id integer NOT NULL DEFAULT nextval('sponsor_id_seq'),
    name varchar(255),
    description text,
    website varchar(255),
    is_general_sponsor boolean,
    sponsor_order integer,
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE hackathon_sponsor_id_seq;
CREATE TABLE hackathons_sponsors (
	id integer NOT NULL DEFAULT nextval('hackathon_sponsor_id_seq'),
	sponsor_order integer,
	
	hackathon_id integer,
	sponsor_id integer,
	
	FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
	FOREIGN KEY (sponsor_id) REFERENCES sponsors(id),
	PRIMARY KEY (id)
)

# --- !Downs
DROP TABLE hackathons_sponsors CASCADE;
DROP SEQUENCE hackathon_sponsor_id_seq;

DROP TABLE sponsors CASCADE;
DROP SEQUENCE sponsor_id_seq;

DROP TABLE prizes CASCADE;
DROP SEQUENCE prize_id_seq;

DROP TABLE news_labels;
DROP SEQUENCE news_label_id_seq;

DROP TABLE labels;
DROP SEQUENCE label_id_seq;

DROP TABLE news CASCADE;
DROP SEQUENCE news_id_seq;

DROP TABLE users_teams;
DROP SEQUENCE user_team_id_seq;

DROP TABLE teams CASCADE;
DROP SEQUENCE team_id_seq;

DROP TABLE problems CASCADE;
DROP SEQUENCE problem_id_seq;

DROP TABLE hackathons CASCADE;
DROP SEQUENCE hackathons_id_seq;

DROP TABLE locations CASCADE;
DROP SEQUENCE location_id_seq;

DROP TABLE users_roles CASCADE;
DROP SEQUENCE user_role_id_seq;

DROP TABLE roles CASCADE;
DROP SEQUENCE role_id_seq;

DROP TABLE users CASCADE;
DROP SEQUENCE user_id_seq;
