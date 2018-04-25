--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Columns for block identity after X unsuccessful login atemps


ALTER TABLE idm_password ADD COLUMN block_login_date timestamp without time zone;

ALTER TABLE idm_password_a ADD COLUMN block_login_date timestamp without time zone;

ALTER TABLE idm_password_a ADD COLUMN block_login_date_m boolean;

ALTER TABLE idm_password_policy ADD COLUMN block_login_time integer;

ALTER TABLE idm_password_policy ADD COLUMN max_unsuccessful_attempts integer;

ALTER TABLE idm_password_policy_a ADD COLUMN block_login_time integer;

ALTER TABLE idm_password_policy_a ADD COLUMN block_login_time_m boolean;

ALTER TABLE idm_password_policy_a ADD COLUMN max_unsuccessful_attempts integer;

ALTER TABLE idm_password_policy_a ADD COLUMN max_unsuccessful_attempts_m boolean;
