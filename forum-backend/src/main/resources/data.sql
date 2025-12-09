
--
insert into app_users(id, username, name, surname, password, role)
    values ('00000000-0000-0000-0000-000000000001', 'test',  'tName', 'tSurname', '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.', 'ROOT');
--  password evaluates to 'test'
--
-----------------------------------------
-- REGULAR USERS (ROLE = USER)
-----------------------------------------
INSERT INTO app_users (id, username, name, surname, email, password, phone, created_at, role)
VALUES
    ('22222222-2222-2222-2222-222222222222',
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
     'USER');

-----------------------------------------
-- USER GROUPS (OWNED BY REGULAR USERS)
-----------------------------------------
-- Group 1: owned by janek
INSERT INTO user_group (id, owner_id, name, created_at)
VALUES (
           'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
           '22222222-2222-2222-2222-222222222222',
           'Lubelskie Spinning Team',
           '2025-02-01T08:00:00Z'
       );

-- Group 2: owned by kasia
INSERT INTO user_group (id, owner_id, name, created_at)
VALUES (
           'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
           '33333333-3333-3333-3333-333333333333',
           'Nocne Wypady Na Sumy',
           '2025-02-02T19:30:00Z'
       );

-----------------------------------------
-- GROUP ADMINS (GROUP-LEVEL ADMINS, STILL ROLE USER)
-----------------------------------------
-- Group 1 admins: janek (owner), marek as additional admin
INSERT INTO user_group_admins (group_id, user_id) VALUES
                                                      ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222'),
                                                      ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444');

-- Group 2 admins: kasia (owner)
INSERT INTO user_group_admins (group_id, user_id) VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333');

-----------------------------------------
-- GROUP MEMBERS
-- Owner must be a member too (your @PrePersist rule), so we add them explicitly.
-----------------------------------------
-- Group 1 members: janek (owner), kasia, marek
INSERT INTO user_group_members (group_id, user_id) VALUES
                                                       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222'),
                                                       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333'),
                                                       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444');

-- Group 2 members: kasia (owner), janek
INSERT INTO user_group_members (group_id, user_id) VALUES
                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333'),
                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222');

-----------------------------------------
-- OPTIONAL: CANDIDATES
-- Example: marek requested to join Group 2
-----------------------------------------
INSERT INTO user_group_candidates (group_id, user_id) VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '44444444-4444-4444-4444-444444444444');


INSERT INTO content (id, author_id, content, created_at, content_type, parent_id)
VALUES
    (1, '00000000-0000-0000-0000-000000000001', 'Pierwszy testowy post na forum.',        '2025-01-01T10:00:00Z', 'POST',   NULL),
    (2, '33333333-3333-3333-3333-333333333333', 'Drugi testowy post o ulubionych łowiskach.', '2025-01-02T12:30:00Z', 'POST',   NULL),
    (3, '00000000-0000-0000-0000-000000000001', 'Post o sprzęcie wędkarskim.',            '2025-01-03T09:15:00Z', 'POST',   NULL);

-- Comment under post #1 (child content)
INSERT INTO content (id, author_id, content, created_at, content_type, parent_id)
VALUES
    (4, '33333333-3333-3333-3333-333333333333', 'Komentarz pod pierwszym postem.',        '2025-01-03T10:00:00Z', 'COMMENT', 1);

