--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Forbidden characters which passwords must not start/end with.

ALTER TABLE idm_password_policy ADD prohibited_begin_characters NVARCHAR(255);
ALTER TABLE idm_password_policy_a ADD prohibited_begin_characters NVARCHAR(255);
ALTER TABLE idm_password_policy_a ADD prohibited_begin_characters_m bit;

ALTER TABLE idm_password_policy ADD prohibited_end_characters NVARCHAR(255);
ALTER TABLE idm_password_policy_a ADD prohibited_end_characters NVARCHAR(255);
ALTER TABLE idm_password_policy_a ADD prohibited_end_characters_m bit;
