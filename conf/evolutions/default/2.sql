# Metadata

# --- !Ups 

insert into roles (name) values ('admin');
insert into roles (name) values ('user');

insert into users (name, email, github_username, open_id, avatar_url) values ('Łukasz Budnik','l.budnik@kainos.com', 'lukasz-budnik', '108382012254355503021google', 'https://lh4.googleusercontent.com/--E57EZJfFuE/AAAAAAAAAAI/AAAAAAAADMk/4soRdJYOuoQ/s250-c-k/photo.jpg');

insert into users_roles (user_id, role_id) values (1, 1);

# --- !Downs

delete from roles where name in ('admin', 'user');

delete from users where github_username = 'lukasz-budnik';
