--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add profile setting properties.

ALTER TABLE idm_profile ADD COLUMN system_information boolean NOT NULL DEFAULT false;
ALTER TABLE idm_profile_a ADD COLUMN system_information boolean;
ALTER TABLE idm_profile_a ADD COLUMN system_information_m bool;

ALTER TABLE idm_profile ADD COLUMN default_page_size integer;
ALTER TABLE idm_profile_a ADD COLUMN default_page_size integer;
ALTER TABLE idm_profile_a ADD COLUMN default_page_size_m bool;