# Last evolution contains ONLY test data
# this evolution is removed when deploying to Heroku!

# --- !Ups 

insert into users (name, email, github_username, open_id) values ('Lukasz Budnik','email', 'lukasz-budnik', '108382012254355503021google');
insert into users (name, email, github_username, open_id) values ('test','email', 'test', 'open_id_test');

insert into roles (name) values ('admin');

insert into users_roles (user_id, role_id) values (1, 1);

insert into news (title, text, published, author_id) values ('Tytuł', 'This is an example <!--more-->of multiline news', '2012-01-01 00:00:00', 1);
insert into news (title, text, published, author_id) values ('Tytuł 2', 'This is an example <!--more-->of multiline news', '2012-01-02 00:00:00', 1);

insert into labels (value) values ('test_label_1');
insert into labels (value) values ('test_label_2');
insert into labels (value) values ('test_label_3');

insert into news_labels (news_id, label_id) values (1, 1);
insert into news_labels (news_id, label_id) values (1, 2);
insert into news_labels (news_id, label_id) values (1, 3);
insert into news_labels (news_id, label_id) values (2, 1);
insert into news_labels (news_id, label_id) values (2, 2);

insert into locations (country, city, postal_code, full_address, name, latitude, longitude) values ('Polska', 'Gdańsk', '80-000', 'Ul. Długa 55', 'Hackaton venue!', 54.366667, 18.633333);
insert into hackathons (subject, status, submitter_id, location_id) values ('Hackaton testowy', 1, 1, 1);

insert into locations (country, city, postal_code, full_address, name, latitude, longitude) values ('Polska', 'Warszawa', '80-000', 'Ul. Krótka 5', 'Hackaton venue!', 52.2323, 21.008433);
insert into hackathons (subject, status, submitter_id, location_id) values ('Hackaton testowy', 2, 1, 2);


insert into news (title, text, published, author_id, hackathon_id) values ('Hackathon News 1', 'This is an example <!--more-->of multiline news', '2012-01-01 00:00:00', 1, 1);
insert into news (title, text, published, author_id, hackathon_id) values ('Hackathon News 2', 'This is an example <!--more-->of multiline news', '2012-01-02 00:00:00', 1, 1);

insert into labels (value) values ('hackathon_1_test_label_1');
insert into labels (value) values ('hackathon_1_test_label_2');

insert into news_labels (news_id, label_id) values (3, 4);
insert into news_labels (news_id, label_id) values (4, 5);

insert into problems (name, description, submitter_id, hackathon_id) values ('Problem 1.', 'Problem testowy', 1, 1);
insert into problems (name, description, submitter_id, hackathon_id) values ('Problem 2.', 'Problem testowy numer 2.', 1, 1);

insert into teams (name, creator_id, hackathon_id) values ('Team 1.', 1, 1);
insert into teams (name, creator_id, hackathon_id) values ('Team 2.', 2, 1);

insert into users_teams (user_id, team_id) values (1, 1);
insert into users_teams (user_id, team_id) values (2, 1);

insert into prizes (name, description, prize_order, founder_name, founder_web_page, hackathon_id) values ('Prize 1', 'Fantastic prize', 1, 'Kainos Software Ltd', 'http://www.kainos.pl', 1);
insert into prizes (name, description, prize_order, founder_name, founder_web_page, hackathon_id) values ('Prize 2', 'Awesome prize', 2, 'Kainos Software Ltd', 'http://www.kainos.pl', 1);
insert into prizes (name, description, prize_order, hackathon_id) values ('Prize 3', 'Cool prize', 3, 1);

insert into sponsors (name, description, website, is_general_sponsor, sponsor_order) values ('Kainos Software Ltd', 'First main sponsor', 'www.google.com', TRUE, 1);
insert into sponsors (name, description, website, is_general_sponsor, sponsor_order) values ('General sponsor 2', 'Second main sponsor + Hackaton sponsor', 'www.google.com', TRUE, 2);
insert into sponsors (name, description, website, is_general_sponsor, sponsor_order) values ('Hackaton Sponsor 1', 'Hackaton sponsor description 1', 'www.google.com', FALSE, 3);
insert into sponsors (name, description, website, is_general_sponsor, sponsor_order) values ('Hackaton Sponsor 2', 'Hackaton sponsor description 1', 'www.google.com', FALSE, 3);
insert into sponsors (name, description, website, is_general_sponsor, sponsor_order) values ('Hackaton Sponsor 3', 'Hackaton sponsor description 1', 'www.google.com', FALSE, 3);

insert into hackathons_sponsors(hackathon_id, sponsor_id, sponsor_order) values (1,2,1);
insert into hackathons_sponsors(hackathon_id, sponsor_id, sponsor_order) values (1,3,2);
insert into hackathons_sponsors(hackathon_id, sponsor_id, sponsor_order) values (1,4,3);
insert into hackathons_sponsors(hackathon_id, sponsor_id, sponsor_order) values (1,5,3);

insert into hackathons_sponsors(hackathon_id, sponsor_id, sponsor_order) values (2,2,1);
insert into hackathons_sponsors(hackathon_id, sponsor_id, sponsor_order) values (2,4,2);
insert into hackathons_sponsors(hackathon_id, sponsor_id, sponsor_order) values (2,5,3);

# --- !Downs

delete from hackathons_sponsors;

delete from sponsors;

delete from prizes;

delete from users_roles;

delete from users_teams;

delete from teams;

delete from problems;
    
delete from hackathons;

delete from locations;

delete from news;
    
delete from users;
