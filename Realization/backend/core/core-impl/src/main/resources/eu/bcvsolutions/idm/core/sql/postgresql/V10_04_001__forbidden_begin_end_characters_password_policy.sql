--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Forbidden characters which passwords must not start/end with.

ALTER TABLE idm_password_policy ADD COLUMN prohibited_begin_characters varchar(255);
ALTER TABLE idm_password_policy_a ADD COLUMN prohibited_begin_characters varchar(255);
ALTER TABLE idm_password_policy_a ADD COLUMN prohibited_begin_characters_m boolean;

ALTER TABLE idm_password_policy ADD COLUMN prohibited_end_characters varchar(255);
ALTER TABLE idm_password_policy_a ADD COLUMN prohibited_end_characters varchar(255);
ALTER TABLE idm_password_policy_a ADD COLUMN prohibited_end_characters_m boolean;
