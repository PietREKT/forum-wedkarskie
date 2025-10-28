-- insert into app_roles(name, perm_level) select 'USER', 1 where not exists(
--     select 1 from app_roles r where r.name = 'USER'
-- );
-- insert into app_roles(name, perm_level) select 'OWNER', 2 where not exists(
--     select 1 from app_roles r where r.name = 'OWNER'
-- );
-- insert into app_roles(name, perm_level) select 'MOD', 3 where not exists(
--     select 1 from app_roles r where r.name = 'MOD'
-- );
-- insert into app_roles(name, perm_level) select 'ADMIN', 4 where not exists(
--     select 1 from app_roles r where r.name = 'ADMIN'
-- );


insert into app_users(username, name, surname, password, role)
    values ('test',  'tName', 'tSurname', '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.', 'ROOT');
--  password evaluates to 'test'

-- insert into user_roles(user_id, roles_id)
-- select (select u.id from app_users u where u.username = 'test'),
--         (select r.id from app_roles r where r.name = 'ADMIN')
-- where not exists(select 1 from user_roles where user_id=(select u.id from app_users u where u.username = 'test') and roles_id=(select r.id from app_roles r where r.name = 'ADMIN'));