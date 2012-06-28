# Metadata

# --- !Ups 

insert into roles (name) values ('admin');
insert into roles (name) values ('user');

# --- !Downs

delete from roles where name in ('admin', 'user');
