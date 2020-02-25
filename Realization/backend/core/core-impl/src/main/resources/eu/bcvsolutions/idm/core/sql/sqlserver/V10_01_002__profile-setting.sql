--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add profile setting properties.

ALTER TABLE idm_profile ADD system_information bit NOT NULL DEFAULT 0;
ALTER TABLE idm_profile_a ADD system_information bit;
ALTER TABLE idm_profile_a ADD system_information_m bit;

ALTER TABLE idm_profile ADD default_page_size int;
ALTER TABLE idm_profile_a ADD default_page_size int;
ALTER TABLE idm_profile_a ADD default_page_size_m bit;