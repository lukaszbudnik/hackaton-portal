# Database model
 
# --- !Ups

CREATE SEQUENCE prize_id_seq;

CREATE TABLE prizes (
    id integer NOT NULL DEFAULT nextval('prize_id_seq'),
    name varchar(255),
    description text,
    prize_order varchar(128),
    
    hackathon_id integer NOT NULL,
    
    FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE prizes CASCADE;
DROP SEQUENCE prize_id_seq;