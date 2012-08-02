# Last evolution contains ONLY test data
# this evolution is removed when deploying to Heroku!

# --- !Ups 

insert into users (name, email, github_username, open_id, is_admin) values ('Jan Kowalski','email', 'test', '102068228977340999495google', true);
insert into users (name, email, github_username, open_id) values ('test user','email', 'test', 'open_id_test');

insert into locations (country, city, postal_code, full_address, name, latitude, longitude) values ('Polska', 'Gdańsk', '80-000', 'Ul. Długa 55', 'Hackaton Gdansk!', 54.366667, 18.633333);
insert into locations (country, city, postal_code, full_address, name, latitude, longitude) values ('Polska', 'Warszawa', '80-000', 'Ul. Krótka 5', 'Hackaton Warszawa!', 52.2323, 21.008433);

insert into hackathons (subject, date, status, organiser_id, location_id) values ('Hackaton testowy 1', '2012-01-02', 1, 1, 1);
insert into hackathons (subject, date, status, organiser_id, location_id) values ('Hackaton testowy 2', '2012-03-04', 2, 2, 2);

insert into problems (name, description, status, submitter_id, hackathon_id) values ('Problem 1', 'Problem testowy numer 1', 1, 1, 1);
insert into problems (name, description, status, submitter_id, hackathon_id) values ('Problem 2', 'Problem testowy numer 2', 2, 1, 1);

insert into teams (name, status, creator_id, hackathon_id) values ('Team 1', 1, 1, 1);
insert into teams (name, status, creator_id, hackathon_id) values ('Team 2', 2, 2, 1);

insert into hackathons_users (hackathon_id, user_id, team_id) values (1, 2, 2);

insert into news (title, text, published_date, author_id) values ('Tytuł 1', 'This is an example <!--more-->of multiline news', '2012-01-01 00:00:00', 1);
insert into news (title, text, published_date, author_id) values ('Tytuł 2', 'This is an example <!--more-->of multiline news', '2012-01-02 00:00:00', 1);

insert into labels (value) values ('test_label_1');
insert into labels (value) values ('test_label_2');
insert into labels (value) values ('test_label_3');

insert into news_labels (news_id, label_id) values (1, 1);
insert into news_labels (news_id, label_id) values (1, 2);
insert into news_labels (news_id, label_id) values (1, 3);
insert into news_labels (news_id, label_id) values (2, 1);
insert into news_labels (news_id, label_id) values (2, 2);

insert into news (title, text, published_date, author_id, hackathon_id) values ('Hackathon News 1', 'This is an example <!--more-->of multiline news', '2012-01-01 00:00:00', 1, 1);
insert into news (title, text, published_date, author_id, hackathon_id) values ('Hackathon News 2', 'This is an example <!--more-->of multiline news', '2012-01-02 00:00:00', 1, 1);

insert into labels (value) values ('hackathon_1_test_label_1');
insert into labels (value) values ('hackathon_1_test_label_2');

insert into news_labels (news_id, label_id) values (3, 4);
insert into news_labels (news_id, label_id) values (4, 5);

insert into prizes (name, description, prize_order, founder_name, founder_web_page, hackathon_id) values ('Prize 1', 'Fantastic prize', 1, 'Kainos Software Ltd', 'http://www.kainos.pl', 1);
insert into prizes (name, description, prize_order, founder_name, founder_web_page, hackathon_id) values ('Prize 2', 'Awesome prize', 2, 'Kainos Software Ltd', 'http://www.kainos.pl', 1);
insert into prizes (name, description, prize_order, hackathon_id) values ('Prize 3', 'Cool prize', 3, 1);

insert into sponsors (name, title, description, website, sponsor_order) values ('Kainos Software Ltd', 'First main sponsor', 'Some nice sponsor', 'www.google.com', 1);
insert into sponsors (name, title, description, website, sponsor_order) values ('General sponsor 2', 'Second main sponsor', 'Another sponsor', 'www.google.com', 2);
insert into sponsors (name, title, description, website, sponsor_order, hackathon_id) values ('Hackaton Sponsor 1', 'Gold', 'Hackaton sponsor description 1', 'www.google.com', 3, 1);
insert into sponsors (name, title, description, website, sponsor_order, hackathon_id) values ('Hackaton Sponsor 2', 'Silver', 'Hackaton sponsor description 1', 'www.google.com', 3, 1);
insert into sponsors (name, title, description, website, sponsor_order, hackathon_id) values ('Hackaton Sponsor 3', 'Bronze', 'Hackaton sponsor description 1', 'www.google.com', 3, 1);

# --- !Downs

delete from sponsors;

delete from prizes;

delete from news_labels;

delete from labels;

delete from news;

delete from hackathons_users;

delete from teams;

delete from problems;
    
delete from hackathons;

delete from locations;

delete from users where github_username != 'lukasz-budnik'
