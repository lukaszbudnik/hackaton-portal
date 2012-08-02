# Database model
 
# --- !Ups

CREATE SEQUENCE user_id_seq;
CREATE TABLE users (
    id integer NOT NULL DEFAULT nextval('user_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    name varchar(255),
    email varchar(255) NOT NULL,
	avatar_url text,    
    open_id varchar(255),
    github_username varchar(255),    
	twitter_account text,
	is_admin bool DEFAULT false,
	is_blocked bool DEFAULT false,
	
    PRIMARY KEY (id)
);

CREATE SEQUENCE location_id_seq;
CREATE TABLE locations (
    id integer NOT NULL DEFAULT nextval('location_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    country varchar(255),
    city varchar(255),
    postal_code varchar(255),
    full_address text,
    name varchar(255),
	latitude float8,
	longitude float8,
	    
    PRIMARY KEY (id)
);

CREATE SEQUENCE hackathon_id_seq;
CREATE TABLE hackathons (
    id integer NOT NULL DEFAULT nextval('hackathon_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    date timestamp,
    subject varchar(255),
    status integer NOT NULL,
    
    organiser_id integer NOT NULL,
    location_id integer NOT NULL,
    
    FOREIGN KEY (organiser_id) REFERENCES users(id),
    FOREIGN KEY (location_id) REFERENCES locations(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE problem_id_seq;
CREATE TABLE problems (
    id integer NOT NULL DEFAULT nextval('problem_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
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
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    name varchar(255),
    status integer NOT NULL,
    
    creator_id integer NOT NULL,
    hackathon_id integer NOT NULL,
    problem_id integer UNIQUE,
    
    FOREIGN KEY (creator_id) REFERENCES users(id),
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    FOREIGN KEY (problem_id) REFERENCES problems(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE hackathon_user_id_seq;
CREATE TABLE hackathons_users (
    id integer NOT NULL DEFAULT nextval('hackathon_user_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    
	hackathon_id integer NOT NULL,
    user_id integer NOT NULL,
    team_id integer,
    
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    UNIQUE (hackathon_id, user_id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE news_id_seq;
CREATE TABLE news (
    id integer NOT NULL DEFAULT nextval('news_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
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
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    value varchar(255) UNIQUE,
    
    PRIMARY KEY (id)
);

CREATE SEQUENCE news_label_id_seq;
CREATE TABLE news_labels (
    id integer NOT NULL DEFAULT nextval('news_label_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    
    news_id integer NOT NULL,
    label_id integer NOT NULL,
    
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE,
    UNIQUE (news_id, label_id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE prize_id_seq;
CREATE TABLE prizes (
    id integer NOT NULL DEFAULT nextval('prize_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    name varchar(255),
    description text,
    prize_order integer,
    founder_name varchar(255),
    founder_web_page varchar(255),
    
    hackathon_id integer NOT NULL,
    
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    PRIMARY KEY (id)
);

CREATE SEQUENCE resource_id_seq;
CREATE TABLE resources(
	id integer NOT NULL DEFAULT nextval('resource_id_seq'),
	publicId varchar(255),
	url text,
	PRIMARY KEY (id)
);

CREATE SEQUENCE sponsor_id_seq;
CREATE TABLE sponsors (
    id integer NOT NULL DEFAULT nextval('sponsor_id_seq'),
    creation_timestamp timestamp NOT NULL DEFAULT now(),
    name varchar(255),
    title varchar(255),
    description text,
    website varchar(255),
    sponsor_order integer,
    
    hackathon_id integer,
    logo_resource_id integer,   
    
	FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    FOREIGN KEY (logo_resource_id) REFERENCES resources(id),
    PRIMARY KEY (id)
);

# --- !Downs
DROP TABLE sponsors CASCADE;
DROP SEQUENCE sponsor_id_seq;

DROP TABLE resources CASCADE;
DROP SEQUENCE resource_id_seq;

DROP TABLE prizes CASCADE;
DROP SEQUENCE prize_id_seq;

DROP TABLE news_labels;
DROP SEQUENCE news_label_id_seq;

DROP TABLE labels;
DROP SEQUENCE label_id_seq;

DROP TABLE news CASCADE;
DROP SEQUENCE news_id_seq;

DROP TABLE hackathons_users;
DROP SEQUENCE hackathon_user_id_seq;

DROP TABLE teams CASCADE;
DROP SEQUENCE team_id_seq;

DROP TABLE problems CASCADE;
DROP SEQUENCE problem_id_seq;

DROP TABLE hackathons CASCADE;
DROP SEQUENCE hackathons_id_seq;

DROP TABLE locations CASCADE;
DROP SEQUENCE location_id_seq;

DROP TABLE users CASCADE;
DROP SEQUENCE user_id_seq;
