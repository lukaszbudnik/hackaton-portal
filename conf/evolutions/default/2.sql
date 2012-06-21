# Database model
 
# --- !Ups

ALTER TABLE locations ADD COLUMN latitude float8;
ALTER TABLE locations ADD COLUMN longitude float8;

# --- !Downs

ALTER TABLE locations DROP COLUMN latitude;
ALTER TABLE locations DROP COLUMN longitude;



