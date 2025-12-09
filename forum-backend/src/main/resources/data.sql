
--
insert into app_users(id, username, name, surname, password, role)
    values ('00000000-0000-0000-0000-000000000001', 'test',  'tName', 'tSurname', '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.', 'ROOT');
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

-- ======================================================================
-- RYBY (fish)
-- ======================================================================
insert into fish(id, name, species, avg_length, avg_mass, photo_url, description, is_predatory, water_type) values
                                                                                                                (1, 'Szczupak', 'Esox lucius', 70.0, 3.5, '/fish/northern_pike.jpg',
                                                                                                                 'Klasyczny drapieżnik jeziorowy, często spotykany w dużych zbiornikach zaporowych.', true, 'FRESHWATER'),
                                                                                                                (2, 'Karp', 'Cyprinus carpio', 50.0, 2.0, null,
                                                                                                                 'Popularna ryba karpiowa w wodach stojących i wolno płynących.', false, 'FRESHWATER'),
                                                                                                                (3, 'Okoń', 'Perca fluviatilis', 25.0, 0.3, null,
                                                                                                                 'Niewielki drapieżnik żyjący w ławicach, często łowiony na spinning.', true, 'FRESHWATER');

-- ======================================================================
-- METODY POŁOWU DLA RYB (tabela fishing_methods)
-- ======================================================================
insert into fishing_methods(fish_id, method) values
                                                 (1, 'SPINNING'),
                                                 (1, 'LIVE_BAIT'),
                                                 (2, 'GROUND'),
                                                 (2, 'FEEDER'),
                                                 (3, 'SPINNING');

-- ======================================================================
-- ŁOWISKA (tabela fishing_spot)
--  location – POINT(longitude latitude), SRID 4326
--  type – enum FISHING_SPOT_TYPE: PRIVATE / PUBLIC
--  verification_status – enum VerificationStatus: IN_REVIEW / ACCEPTED / REJECTED
-- ======================================================================
insert into fishing_spot(id, name, description, location, type, verification_status, statute_url, owner_id) values
                                                                                                                (1,
                                                                                                                 'Zalew testowy',
                                                                                                                 'Przykładowy zbiornik zaporowy dodany z data.sql – do testów mapy łowisk.',
                                                                                                                 ST_GeomFromText('POINT(22.5400 51.2300)', 4326),
                                                                                                                 'PUBLIC',
                                                                                                                 'ACCEPTED',
                                                                                                                 null,
                                                                                                                 '00000000-0000-0000-0000-000000000001'
                                                                                                                ),
                                                                                                                (2,
                                                                                                                 'Rzeka testowa',
                                                                                                                 'Przykładowy odcinek rzeki z dostępem z brzegu.',
                                                                                                                 ST_GeomFromText('POINT(22.5700 51.2500)', 4326),
                                                                                                                 'PUBLIC',
                                                                                                                 'ACCEPTED',
                                                                                                                 null,
                                                                                                                 '00000000-0000-0000-0000-000000000001'
                                                                                                                );

-- ======================================================================
-- POWIĄZANIA ŁOWISKO–RYBA (tabela fishing_spots_fish)
-- ======================================================================
insert into fishing_spots_fish(spot_id, fish_id) values
                                                     (1, 1),  -- Zalew testowy – Szczupak
                                                     (1, 3),  -- Zalew testowy – Okoń
                                                     (2, 2);  -- Rzeka testowa – Karp
