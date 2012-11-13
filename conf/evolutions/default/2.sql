# Metadata

# --- !Ups 

insert into users (name, email, github_username, open_id, avatar_url, is_admin) values ('Łukasz Budnik','l.budnik@kainos.com', 'lukaszbudnik', '108382012254355503021google', 'https://lh4.googleusercontent.com/--E57EZJfFuE/AAAAAAAAAAI/AAAAAAAADMk/4soRdJYOuoQ/s250-c-k/photo.jpg', true);
insert into users (name, email, github_username, open_id, avatar_url, is_admin) values ('Daniel Bykowski','d.bykowski@kainos.com', 'bykes', '102068228977340999495google', 'https://lh4.googleusercontent.com/-_qzaBoetBfc/AAAAAAAAAAI/AAAAAAAAENc/q3rBQpasMfI/photo.jpg', true);

# --- !Downs

delete from users where github_username = 'lukasz-budnik';
