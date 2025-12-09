create extension if not exists postgis;

create index if not exists idx_app_users_username_lower
    ON app_users (lower(username));

create index if not exists idx_user_groups_name_lower
    on user_group (lower(name));

create index if not exists idx_fishing_spots_name_lower
    on fishing_spot (lower(name));

create index if not exists idx_fish_name_lower
    on fish (lower(name));

create index if not exists idx_event_name_lower
    on event (lower(name));