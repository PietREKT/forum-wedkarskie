--
insert into app_users(id, username, name, surname, password, role)
values (
           '00000000-0000-0000-0000-000000000001',
           'test',
           'tName',
           'tSurname',
           '$2a$10$pVf8bDRJzqBQ75LrR7dhEO00av69.V6ZIVy4Zj.nxW.vUcdVE/z0.',
           'ROOT'
       );
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
-- RYBY (fish) – podstawowe
-- ======================================================================
insert into fish(
    id, name, species, avg_length, avg_mass,
    photo_url, description, is_predatory, water_type
) values
      (1, 'Szczupak', 'Esox lucius', 70.0, 3.5, '/fish/northern_pike.jpg',
       'Klasyczny drapieżnik jeziorowy, często spotykany w dużych zbiornikach zaporowych.',
       true, 'FRESHWATER'),
      (2, 'Karp', 'Cyprinus carpio', 50.0, 2.0, null,
       'Popularna ryba karpiowa w wodach stojących i wolno płynących.',
       false, 'FRESHWATER'),
      (3, 'Okoń', 'Perca fluviatilis', 25.0, 0.3, null,
       'Niewielki drapieżnik żyjący w ławicach, często łowiony na spinning.',
       true, 'FRESHWATER');

-- ======================================================================
-- METODY POŁOWU DLA RYB (tabela fishing_methods) – podstawowe
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
insert into fishing_spot(
    id, name, description, location,
    type, verification_status, statute_url, owner_id
) values
      (
          1,
          'Zalew testowy',
          'Przykładowy zbiornik zaporowy dodany z data.sql – do testów mapy łowisk.',
          ST_GeomFromText('POINT(22.5400 51.2300)', 4326),
          'PUBLIC',
          'ACCEPTED',
          null,
          '00000000-0000-0000-0000-000000000001'
      ),
      (
          2,
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

-- ======================================================================
-- DODATKOWE RYBY
-- ======================================================================
insert into fish(
    id, name, species, avg_length, avg_mass,
    photo_url, description, is_predatory, water_type
) values
      (4, 'Sandacz', 'Sander lucioperca', 60.0, 2.5, null,
       'Drapieżnik nocny żerujący głównie w głębszych partiach zbiorników i rzek.',
       true, 'FRESHWATER'),
      (5, 'Leszcz', 'Abramis brama', 40.0, 1.0, null,
       'Ryba spokojnego żeru zasiedlająca muliste dna jezior i wolnych rzek.',
       false, 'FRESHWATER'),
      (6, 'Sum', 'Silurus glanis', 120.0, 15.0, null,
       'Największy drapieżnik naszych wód, aktywny głównie nocą.',
       true, 'FRESHWATER'),
      (7, 'Pstrąg potokowy', 'Salmo trutta m. fario', 30.0, 0.4, null,
       'Ryba łososiowata zasiedlająca czyste, dobrze natlenione rzeki i potoki.',
       true, 'FRESHWATER'),
      (8, 'Lin', 'Tinca tinca', 30.0, 0.7, null,
       'Ryba spokojnego żeru lubiąca muliste, zarośnięte zatoki i trzcinowiska.',
       false, 'FRESHWATER'),
      (9, 'Amur', 'Ctenopharyngodon idella', 60.0, 3.0, null,
       'Roślinożerna ryba karpiowata, często zarybieniowa do walki z roślinnością.',
       false, 'FRESHWATER'),
      (10, 'Węgorz', 'Anguilla anguilla', 70.0, 1.5, null,
       'Nocny drapieżnik o wężowatym kształcie ciała, żeruje przy dnie.',
       true, 'FRESHWATER');

-- ======================================================================
-- METODY POŁOWU DLA DODATKOWYCH RYB
-- ======================================================================
insert into fishing_methods(fish_id, method) values
                                                 (4, 'SPINNING'),
                                                 (4, 'LIVE_BAIT'),
                                                 (5, 'GROUND'),
                                                 (5, 'FEEDER'),
                                                 (6, 'SPINNING'),
                                                 (6, 'LIVE_BAIT'),
                                                 (7, 'SPINNING'),
                                                 (7, 'FLY'),
                                                 (8, 'FLOAT'),
                                                 (8, 'GROUND'),
                                                 (9, 'FLOAT'),
                                                 (9, 'GROUND'),
                                                 (10, 'GROUND'),
                                                 (10, 'LIVE_BAIT');

-- ======================================================================
-- PORADNIKI (CONTENT + TUTORIAL + POWIĄZANIA)
-- ======================================================================

-- ----------------------------------------------------------------------
-- 1) Spinning na szczupaka z brzegu zbiornika zaporowego
-- ----------------------------------------------------------------------
insert into content(
    id, author_id, content, created_at, content_type, parent_id
) values
    (
        10001,
        '00000000-0000-0000-0000-000000000001',
        'Spinning na szczupaka z brzegu zbiornika zaporowego

        Sprzęt:
        - wędka 2,40–2,70 m o ciężarze wyrzutowym 10–30 g
        - kołowrotek 3000–4000 z plecionką 0,12–0,16 mm
        - przypon stalowy lub wolframowy

        Miejsca:
        - okolice spadków dna, blaty przy starym korycie rzeki
        - okolice trzcin i zatopionych drzew

        Prowadzenie przynęt:
        - klasyczny opad gumą na główce 10–20 g
        - jerking większymi woblerami
        - wolne prowadzenie obrotówek nad roślinnością

        Wskazówki:
        - najskuteczniejsze są poranki i wieczory przy lekkim wietrze
        - warto zmieniać tempo prowadzenia i kolor przynęt
        - pamiętaj o przestrzeganiu okresów ochronnych i wymiarów minimalnych.',
        '2025-01-01T10:00:00Z',
        'TUTORIAL',
        null
    );

insert into tutorial(
    id, title, verification_status, verified_by_id, rejection_reason
) values
    (
        10001,
        'Spinning na szczupaka z brzegu',
        'ACCEPTED',
        null,
        null
    );

insert into methods_tutorials(tutorial_id, methods) values
    (10001, 'SPINNING');

insert into fish_tutorials(tutorial_id, fish_id) values
                                                     (10001, 1),   -- Szczupak
                                                     (10001, 3);   -- Okoń

-- ----------------------------------------------------------------------
-- 2) Grunt na leszcza na rzece nizinnej
-- ----------------------------------------------------------------------
insert into content(
    id, author_id, content, created_at, content_type, parent_id
) values
    (
        10002,
        '00000000-0000-0000-0000-000000000001',
        'Grunt na leszcza na rzece nizinnej

        Stanowisko:
        - wybierz spokojniejszy zakolek z głębszą rynną
        - sprawdź dno ciężarkiem lub koszyczkiem – szukaj twardszych blatów

        Zestaw:
        - wędka feeder 3,3–3,6 m do 90 g
        - koszyczek 30–60 g w zależności od uciągu
        - przypon 0,12–0,16 mm długości 40–80 cm
        - haczyk nr 10–14

        Przynęty:
        - białe robaki, pinka, kukurydza, czerwony robak
        - dobrze działa kanapka: biały + czerwony robak

        Nęcenie:
        - mieszanka z dodatkiem gliny wiążącej i grubszego towaru (kukurydza, pellet)
        - na początku kilka koszyków „na pusto”, później systematyczne donęcanie

        Wskazówki:
        - obserwuj szczytówkę – brania leszcza często są leniwe i rozciągnięte w czasie
        - nie zacinaj zbyt agresywnie
        - staraj się utrzymywać zestaw w jednym miejscu.',
        '2025-01-02T10:00:00Z',
        'TUTORIAL',
        null
    );

insert into tutorial(
    id, title, verification_status, verified_by_id, rejection_reason
) values
    (
        10002,
        'Grunt na leszcza na rzece',
        'ACCEPTED',
        null,
        null
    );

insert into methods_tutorials(tutorial_id, methods) values
                                                        (10002, 'GROUND'),
                                                        (10002, 'FEEDER');

insert into fish_tutorials(tutorial_id, fish_id) values
                                                     (10002, 5),   -- Leszcz
                                                     (10002, 2);   -- Karp

-- ----------------------------------------------------------------------
-- 3) Spławik na lina w zarośniętej zatoce
-- ----------------------------------------------------------------------
insert into content(
    id, author_id, content, created_at, content_type, parent_id
) values
    (
        10003,
        '00000000-0000-0000-0000-000000000001',
        'Spławik na lina w zarośniętej zatoce

        Miejsce:
        - płytka, zarośnięta zatoka z trzcinami i grążelami
        - głębokość 0,8–1,5 m, spokojna woda

        Zestaw:
        - wędka typu match lub odległościówka 3,6–4,2 m
        - żyłka główna 0,18–0,20 mm
        - spławik 1–2 g wyważony śrucinami
        - przypon 0,14–0,16 mm, haczyk nr 10–12

        Przynęty:
        - kukurydza, czerwony robak, rosówka pocięta na kawałki
        - dobrze działa również pęczak i ciasto

        Nęcenie:
        - kulki z zanęty i gliny z dodatkiem kukurydzy, konopi i pęczaku
        - lepiej częściej, mniejszymi porcjami niż jednorazowo za dużo

        Wskazówki:
        - lina najlepiej łowi się o świcie i o zmierzchu
        - po delikatnym położeniu przynęty odczekaj – brania często są powolne
        - przy holu nie forsuj zestawu, ryba ma sporo siły i lubi wchodzić w rośliny.',
        '2025-01-03T10:00:00Z',
        'TUTORIAL',
        null
    );

insert into tutorial(
    id, title, verification_status, verified_by_id, rejection_reason
) values
    (
        10003,
        'Spławik na lina przy trzcinach',
        'ACCEPTED',
        null,
        null
    );

insert into methods_tutorials(tutorial_id, methods) values
    (10003, 'FLOAT');

insert into fish_tutorials(tutorial_id, fish_id) values
                                                     (10003, 8),   -- Lin
                                                     (10003, 2);   -- Karp
