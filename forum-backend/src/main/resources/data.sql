--
insert into app_users(id, username, name, surname, password, role)
values ('00000000-0000-0000-0000-000000000001', 'test', 'tName', 'tSurname',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.', 'ROOT');
--  password evaluates to 'test'
--
-----------------------------------------
-- REGULAR USERS (ROLE = USER)
-----------------------------------------
INSERT INTO app_users (id, username, name, surname, email, password, phone, created_at, role)
VALUES ('22222222-2222-2222-2222-222222222222',
        'janek',
        'Jan',
        'Kowalski',
        'janek@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48222222222',
        '2025-01-02T11:15:00Z',
        'USER'),

       ('33333333-3333-3333-3333-333333333333',
        'kasia',
        'Katarzyna',
        'Nowak',
        'kasia@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48333333333',
        '2025-01-03T09:45:00Z',
        'USER'),

       ('44444444-4444-4444-4444-444444444444',
        'marek',
        'Marek',
        'Wiśniewski',
        'marek@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48444444444',
        '2025-01-04T14:20:00Z',
        'USER'),

       ('55555555-5555-5555-5555-555555555555',
        'janek2',
        'Jan',
        'Nowicki',
        'janek2@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48555555555',
        '2025-01-05T10:05:00Z',
        'USER'),

       ('66666666-6666-6666-6666-666666666666',
        'janusz',
        'Janusz',
        'Kowal',
        'janusz@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48666666666',
        '2025-01-06T12:30:00Z',
        'USER'),

       ('77777777-7777-7777-7777-777777777777',
        'janina',
        'Janina',
        'Mazur',
        'janina@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48777777777',
        '2025-01-07T09:15:00Z',
        'USER'),

       ('88888888-8888-8888-8888-888888888888',
        'marek2',
        'Marek',
        'Lewandowski',
        'marek2@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48888888888',
        '2025-01-08T16:45:00Z',
        'USER'),

       ('99999999-9999-9999-9999-999999999999',
        'kasiula',
        'Kasia',
        'Baran',
        'kasiula@example.com',
        '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
        '+48999999999',
        '2025-01-09T18:20:00Z',
        'USER');

-----------------------------------------
-- USER GROUPS (OWNED BY REGULAR USERS)
-----------------------------------------
-- Group 1: owned by janek
INSERT INTO user_group (id, owner_id, name, created_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '22222222-2222-2222-2222-222222222222',
        'Lubelskie Spinning Team',
        '2025-02-01T08:00:00Z');

-- Group 2: owned by kasia
INSERT INTO user_group (id, owner_id, name, created_at)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        '33333333-3333-3333-3333-333333333333',
        'Nocne Wypady Na Sumy',
        '2025-02-02T19:30:00Z');

-----------------------------------------
-- GROUP ADMINS (GROUP-LEVEL ADMINS, STILL ROLE USER)
-----------------------------------------
-- Group 1 admins: janek (owner), marek as additional admin
INSERT INTO user_group_admins (group_id, user_id)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222'),
       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444');

-- Group 2 admins: kasia (owner)
INSERT INTO user_group_admins (group_id, user_id)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333');

-----------------------------------------
-- GROUP MEMBERS
-- Owner must be a member too
-----------------------------------------
-- Group 1 members: janek (owner), kasia, marek, root
INSERT INTO user_group_members (group_id, user_id)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222'),
       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333'),
       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444'),
       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '00000000-0000-0000-0000-000000000001');

-- Group 2 members: kasia (owner), janek
INSERT INTO user_group_members (group_id, user_id)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333'),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222');

-----------------------------------------
-- OPTIONAL: CANDIDATES
-- Example: marek requested to join Group 2
-----------------------------------------
INSERT INTO user_group_candidates (group_id, user_id)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '44444444-4444-4444-4444-444444444444');


------------------------------------------------------------
--  POSTS / COMMENTS (CONTENT)
------------------------------------------------------------
INSERT INTO content (author_id, content, created_at, content_type, parent_id)
VALUES ('00000000-0000-0000-0000-000000000001',
        'Pierwszy testowy post na forum.',
        '2025-01-01T10:00:00Z',
        'POST',
        NULL),
       ('33333333-3333-3333-3333-333333333333',
        'Drugi testowy post o ulubionych łowiskach.',
        '2025-01-02T12:30:00Z',
        'POST',
        NULL),
       ('00000000-0000-0000-0000-000000000001',
        'Post o sprzęcie wędkarskim.',
        '2025-01-03T09:15:00Z',
        'POST',
        NULL);

-- Comment under post #1 (child content)
INSERT INTO content (author_id, content, created_at, content_type, parent_id)
VALUES ('33333333-3333-3333-3333-333333333333',
        'Komentarz pod pierwszym postem.',
        '2025-01-03T10:00:00Z',
        'COMMENT',
        1);

------------------------------------------------------------
--  FISH (COMMON SPECIES)
------------------------------------------------------------
INSERT INTO fish (id, name, species, avg_length, avg_mass, photo_url, description, is_predatory, water_type)
VALUES
    (1,
     'Sandacz',
     'Sander lucioperca',
     60.0,
     3.0,
     'https://example.com/sandacz.jpg',
     'Drapieżna ryba, lubi głębszą wodę.',
     true,
     'FRESHWATER'),

    (2,
     'Karp',
     'Cyprinus carpio',
     50.0,
     4.5,
     'https://example.com/karp.jpg',
     'Klasyk komercyjnych łowisk.',
     false,
     'SWEETWATER'),

    (3,
     'Okoń',
     'Perca fluviatilis',
     25.0,
     0.3,
     'https://example.com/okon.jpg',
     'Stadny drapieżnik, częsty w jeziorach i rzekach.',
     true,
     'BOTH'),

    (4,
     'Amur',
     'Ctenopharyngodon idella',
     70.0,
     5.0,
     'https://example.com/amur.jpg',
     'Roślinożerna ryba często spotykana na łowiskach specjalnych.',
     false,
     'FRESHWATER'),

    (5,
     'Leszcz',
     'Abramis brama',
     35.0,
     1.5,
     'https://example.com/leszcz.jpg',
     'Ryba spokojnego żeru, popularna w wodach stojących.',
     false,
     'FRESHWATER'),

    (6,
     'Lin',
     'Tinca tinca',
     30.0,
     1.2,
     'https://example.com/lin.jpg',
     'Lubi muliste dno i ciepłą wodę.',
     false,
     'SWEETWATER'),

    (7,
     'Sum',
     'Silurus glanis',
     120.0,
     15.0,
     'https://example.com/sum.jpg',
     'Duży drapieżnik, aktywny głównie nocą.',
     true,
     'FRESHWATER'),

    (8,
     'Jaź',
     'Leuciscus idus',
     40.0,
     1.0,
     'https://example.com/jaz.jpg',
     'Ryba rzeczna, często łowiona w nurcie.',
     false,
     'FRESHWATER'),

    (9,
     'Kleń',
     'Squalius cephalus',
     35.0,
     1.0,
     'https://example.com/klen.jpg',
     'Rzeczny wszystkożerca, dobry cel na spinning.',
     false,
     'FRESHWATER'),

    (10,
     'Płoć',
     'Rutilus rutilus',
     20.0,
     0.2,
     'https://example.com/ploc.jpg',
     'Jedna z najczęściej spotykanych ryb spokojnego żeru w Polsce.',
     false,
     'FRESHWATER');

------------------------------------------------------------
--  FISHING METHODS (ElementCollection -> fishing_methods)
-- table: fishing_methods (fish_id, method)
------------------------------------------------------------
INSERT INTO fishing_methods (fish_id, method) VALUES
                                                  (1, 'SPINNING'),
                                                  (1, 'FEEDER'),
                                                  (2, 'FLOAT'),
                                                  (2, 'FEEDER'),
                                                  (3, 'SPINNING'),
                                                  (3, 'FLOAT'),
                                                  (4, 'FLOAT'),
                                                  (4, 'FEEDER'),
                                                  (5, 'FEEDER'),
                                                  (5, 'FLOAT'),
                                                  (6, 'FLOAT'),
                                                  (7, 'SPINNING'),
                                                  (7, 'FEEDER'),
                                                  (8, 'FLOAT'),
                                                  (9, 'SPINNING'),
                                                  (10, 'FLOAT'),
                                                  (10, 'FEEDER');

------------------------------------------------------------
--  FISHING SPOTS (MULTIPLE TEST SPOTS FOR MAP)
--  assumes PostGIS + SRID 4326, ST_MakePoint(lng, lat)
------------------------------------------------------------
INSERT INTO fishing_spot (
    name,
    description,
    location,
    type,
    verification_status,
    statute_url,
    owner_id
) VALUES
      -- Spot 1: Łowisko Testowe (jak było)
      ('Łowisko Testowe',
       'Małe prywatne łowisko używane do testów aplikacji.',
       ST_SetSRID(ST_MakePoint(22.5667, 51.2500), 4326),
       'PRIVATE',
       'IN_REVIEW',
       'https://example.com/statute.pdf',
       '00000000-0000-0000-0000-000000000001'),

      -- Spot 2: Jezioro Łabędzie
      ('Jezioro Łabędzie',
       'Duże jezioro z pomostami i dobrym dostępem do brzegu.',
       ST_SetSRID(ST_MakePoint(21.5692, 53.8235), 4326),
       'PUBLIC',
       'ACCEPTED',
       NULL,
       NULL),

      -- Spot 3: Staw Rybny Pod Dębem
      ('Staw Rybny Pod Dębem',
       'Niewielki staw prywatny, regularnie zarybiany.',
       ST_SetSRID(ST_MakePoint(22.5681, 51.2405), 4326),
       'PRIVATE',
       'ACCEPTED',
       NULL,
       '22222222-2222-2222-2222-222222222222'),

      -- Spot 4: Rzeka Bystra
      ('Rzeka Bystra',
       'Szybki nurt, idealne miejsce do połowu kleni i jazi.',
       ST_SetSRID(ST_MakePoint(22.0121, 49.8274), 4326),
       'PUBLIC',
       'ACCEPTED',
       NULL,
       NULL),

      -- Spot 5: Zbiornik Wodny Młyńskie Oko
      ('Zbiornik Wodny Młyńskie Oko',
       'Sztuczny zbiornik z głęboką wodą i dużymi drapieżnikami.',
       ST_SetSRID(ST_MakePoint(18.1233, 54.4212), 4326),
       'PUBLIC',
       'ACCEPTED',
       NULL,
       '33333333-3333-3333-3333-333333333333');

------------------------------------------------------------
--  LINK FISH TO SPOTS (ManyToMany -> fishing_spots_fish)
--  zakładamy, że ID spotów to 1..5 w kolejności powyżej
------------------------------------------------------------
INSERT INTO fishing_spots_fish (spot_id, fish_id) VALUES
                                                      -- Łowisko Testowe (id = 1)
                                                      (1, 1),
                                                      (1, 2),
                                                      (1, 3),

                                                      -- Jezioro Łabędzie (id = 2)
                                                      (2, 1),
                                                      (2, 3),
                                                      (2, 7),

                                                      -- Staw Rybny Pod Dębem (id = 3)
                                                      (3, 2),
                                                      (3, 4),
                                                      (3, 6),

                                                      -- Rzeka Bystra (id = 4)
                                                      (4, 8),
                                                      (4, 9),
                                                      (4, 10),

                                                      -- Zbiornik Młyńskie Oko (id = 5)
                                                      (5, 1),
                                                      (5, 3),
                                                      (5, 7);

------------------------------------------------------------
--  EVENT (1 example event on spot_id = 1)
------------------------------------------------------------
INSERT INTO event (
    name,
    description,
    starts_at,
    ends_at,
    spot_id,
    creator_id,
    group_id
) VALUES
    ('Testowe spotkanie nad wodą',
     'Pierwsze testowe wydarzenie na łowisku testowym.',
     '2025-05-01T08:00:00Z',
     '2025-05-01T16:00:00Z',
     1,
     '00000000-0000-0000-0000-000000000001',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa');

------------------------------------------------------------
--  EVENT PARTICIPANTS (UserEvent)
------------------------------------------------------------
INSERT INTO user_events (event_id, user_id, status) VALUES
                                                        (1, '22222222-2222-2222-2222-222222222222', 'CONFIRMED'),
                                                        (1, '33333333-3333-3333-3333-333333333333', 'INVITED');
