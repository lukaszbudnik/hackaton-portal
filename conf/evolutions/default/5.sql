# Database model
 
# --- !Ups

CREATE SEQUENCE user_team_id_seq;

CREATE TABLE users_teams (
    id integer NOT NULL DEFAULT nextval('user_team_id_seq'),
    
    user_id integer NOT NULL,
    team_id integer NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (team_id) REFERENCES teams(id),
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE users_teams;
DROP SEQUENCE user_team_id_seq;
