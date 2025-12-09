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
-- Owner must be a member too (your @PrePersist rule), so we add them explicitly.
-----------------------------------------
-- Group 1 members: janek (owner), kasia, marek
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


INSERT INTO content (author_id, content, created_at, content_type, parent_id)
VALUES ('00000000-0000-0000-0000-000000000001', 'Pierwszy testowy post na forum.', '2025-01-01T10:00:00Z', 'POST',
        NULL),
       ( '33333333-3333-3333-3333-333333333333', 'Drugi testowy post o ulubionych łowiskach.', '2025-01-02T12:30:00Z',
        'POST', NULL),
       ('00000000-0000-0000-0000-000000000001', 'Post o sprzęcie wędkarskim.', '2025-01-03T09:15:00Z', 'POST', NULL);

-- Comment under post #1 (child content)
INSERT INTO content ( author_id, content, created_at, content_type, parent_id)
VALUES ( '33333333-3333-3333-3333-333333333333', 'Komentarz pod pierwszym postem.', '2025-01-03T10:00:00Z', 'COMMENT',
        1);

------------------------------------------------------------
--  FISH (3 example species)
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
     'Stadny drapieżnik.',
     true,
     'BOTH');

------------------------------------------------------------
--  FISHING METHODS (ElementCollection -> fishing_methods)
-- table: fishing_methods (fish_id, method)
------------------------------------------------------------
INSERT INTO fishing_methods (fish_id, method) VALUES
                                                  (1, 'SPINNING'),
                                                  (1, 'FEEDER'),
                                                  (2, 'FLOAT'),
                                                  (2, 'FEEDER'),
                                                  (3, 'SPINNING');


------------------------------------------------------------
--  FISHING SPOT (1 example spot)
------------------------------------------------------------
-- assumes PostGIS + SRID 4326
-- owner_id = '2222...' (existing user)
INSERT INTO fishing_spot (
    name,
    description,
    location,
    type,
    verification_status,
    statute_url,
    owner_id
) VALUES
    ('Łowisko Testowe',
     'Małe prywatne łowisko używane do testów aplikacji.',
     ST_SetSRID(ST_MakePoint(22.5667, 51.2500), 4326),  -- lng, lat
     'PRIVATE',
     'IN_REVIEW',
     'https://example.com/statute.pdf',
     '00000000-0000-0000-0000-000000000001');

------------------------------------------------------------
--  LINK FISH TO SPOT (ManyToMany -> fishing_spots_fish)
------------------------------------------------------------
INSERT INTO fishing_spots_fish (spot_id, fish_id) VALUES
                                                      (1, 1),
                                                      (1, 2),
                                                      (1, 3);

------------------------------------------------------------
--  EVENT (1 example event on that spot)
------------------------------------------------------------
-- creator_id = '2222...' (janek)
-- group_id   = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa' (existing group)
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
-- table: user_events (id, event_id, user_id, status)
-- status values: INVITED / MAYBE / CONFIRMED
INSERT INTO user_events (event_id, user_id, status) VALUES
                                                            ( 1, '22222222-2222-2222-2222-222222222222', 'CONFIRMED'),  -- creator as confirmed
                                                            ( 1, '33333333-3333-3333-3333-333333333333', 'INVITED');   -- another user invited


