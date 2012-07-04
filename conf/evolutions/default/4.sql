# Database model
 
# --- !Ups

ALTER TABLE hackathons DROP COLUMN status;
ALTER TABLE hackathons ADD COLUMN status integer NOT NULL;

# --- !Downs

ALTER TABLE hackathons DROP COLUMN status;
ALTER TABLE hackathons ADD COLUMN status varchar(255);
