# Database model
 
# --- !Ups

ALTER TABLE locations ADD COLUMN latitude float8;
ALTER TABLE locations ADD COLUMN longitude float8;

ALTER TABLE users ADD COLUMN avatar_url text;
ALTER TABLE users ADD COLUMN twitter_account text;

# --- !Downs

ALTER TABLE locations DROP COLUMN latitude;
ALTER TABLE locations DROP COLUMN longitude;

ALTER TABLE users DROP COLUMN avatar_url;
ALTER TABLE users DROP COLUMN twitter_account;



