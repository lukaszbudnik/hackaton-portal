# Database model
 
# --- !Ups

ALTER TABLE locations ADD COLUMN latitude float8;
ALTER TABLE locations ADD COLUMN longitude float8;

insert into users (name, email, github_username, open_id) values ('Łukasz Budnik','email', 'lukasz-budnik', 'open_id');
insert into users (name, email, github_username, open_id) values ('test','email', 'test', 'open_id_test');

insert into news (title, text, labels, published, author_id) values ('Tytuł', 'This is an example <!--more-->of multiline news', 'label1, label2', '2012-01-01 00:00:00', 1);
insert into news (title, text, labels, published, author_id) values ('Tytuł 2', 'This is an example <!--more-->of multiline news', 'label1, label3', '2012-01-02 00:00:00', 1);

insert into locations (country, city, postal_code, full_address, name, latitude, longitude) values ('Polska', 'Gdańsk', '80-000', 'Ul. Długa 55', 'Hackaton venue!', 30.0, 10.0);

insert into hackathons (subject, status, submitter_id, location_id) values ('Hackaton testowy', 'Zapisy trwają!', 1, 1);

insert into problems (name, description, submitter_id, hackathon_id) values ('Problem 1.', 'Problem testowy', 1, 1);
insert into problems (name, description, submitter_id, hackathon_id) values ('Problem 2.', 'Problem testowy numer 2.', 1, 1);
    
# --- !Downs
delete from problems;
    
delete from hackathons;
    
delete from locations;
    
delete from news;
    
delete from users;

ALTER TABLE locations DROP COLUMN latitude;
ALTER TABLE locations DROP COLUMN longitude;



