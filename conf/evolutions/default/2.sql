# Database model
 
# --- !Ups

insert into users (name, email, github_username, open_id) values ('Łukasz Budnik','email', 'lukasz-budnik', 'open_id');
insert into users (name, email, github_username, open_id) values ('test','email', 'test', 'open_id_test');

insert into news (title, text, labels, published, author_id) values ('Tytuł', 'This is an example <!--more-->of multiline news', 'label1, label2', '2012-01-01 00:00:00', 1);
insert into news (title, text, labels, published, author_id) values ('Tytuł 2', 'This is an example <!--more-->of multiline news', 'label1, label3', '2012-01-02 00:00:00', 1);

# --- !Downs

delete from users;