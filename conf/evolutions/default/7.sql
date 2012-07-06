# Database model
 
# --- !Ups

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
	hackathon_id integer,
	sponsor_id integer,
	sponsor_order integer,
	
	FOREIGN KEY (hackathon_id) REFERENCES hackathons(id),
	FOREIGN KEY (sponsor_id) REFERENCES sponsors(id),
	PRIMARY KEY (id)
)

# --- !Downs

DROP TABLE hackathons_sponsors CASCADE;
DROP SEQUENCE hackathon_sponsor_id_seq;

DROP TABLE sponsors CASCADE;
DROP SEQUENCE sponsor_id_seq;
