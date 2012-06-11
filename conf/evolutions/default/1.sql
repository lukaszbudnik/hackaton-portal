# News schema
 
# --- !Ups

CREATE SEQUENCE news_id_seq;
CREATE TABLE news (
    id integer NOT NULL DEFAULT nextval('news_id_seq'),
    title varchar(255),
    text clob,
    published timestamp,
    author varchar(255)
);
 
# --- !Downs
 
DROP TABLE news;
DROP SEQUENCE news_id_seq;
