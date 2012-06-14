# Database model
 
# --- !Ups

insert into users (name, email, github_username, open_id) values ('Lukasz Budnik','email', 'lukasz-budnik', 'open_id');
insert into users (name, email, github_username, open_id) values ('test','email', 'test', 'open_id_test');

# --- !Downs

delete from users;