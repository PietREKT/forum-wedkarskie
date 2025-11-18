
--
insert into app_users(username, name, surname, password, role)
    values ('test',  'tName', 'tSurname', '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.', 'ROOT');
--  password evaluates to 'test'
--
-- insert into post(author_id, content, created_at, rating, dtype)
--     values (1, 'Post zainicjalizowany automatycznie.', '2025-11-07T15:45:00Z', 0, 'Post');
-- insert into post_edit_history (post_id, edited_at, edited_content)
-- values (1, '2025-11-07T15:45:00Z', 'Post zainicjalizowany automatycznie.');
--
-- insert into comment (author_id, created_at, parent_id, post_id, rating, attachment_url, content)
-- values (1, '2025-11-07T15:50:00Z', null, 1, 0, null, 'Komentarz zainicjalizowany automatycznie');
-- insert into comment_edit_history (comment_id, edited_at, edited_content)
-- values (1, '2025-11-07T15:45:00Z', 'Komentarz zainicjalizowany automatycznie.');
--
-- insert into comment (author_id, created_at, parent_id, post_id, rating, attachment_url, content)
-- values (1, '2025-11-07T15:50:00Z', 1, 1, 0, null, 'Odpowiedź zainicjalizowana automatycznie');
-- insert into comment_edit_history (comment_id, edited_at, edited_content)
-- values (2, '2025-11-07T15:45:00Z', 'Odpowiedź zainicjalizowana automatycznie.');