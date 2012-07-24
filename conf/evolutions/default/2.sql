# Metadata

# --- !Ups 

insert into users (name, email, github_username, open_id, avatar_url, is_admin) values ('Łukasz Budnik','l.budnik@kainos.com', 'lukasz-budnik', '108382012254355503021google', 'https://lh4.googleusercontent.com/--E57EZJfFuE/AAAAAAAAAAI/AAAAAAAADMk/4soRdJYOuoQ/s250-c-k/photo.jpg', true);

# --- !Downs

delete from users where github_username = 'lukasz-budnik';
