--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- New feature password never expires

ALTER TABLE idm_password ADD COLUMN password_never_expires bool NOT NULL DEFAULT false;
ALTER TABLE idm_password_a ADD COLUMN password_never_expires bool;
ALTER TABLE idm_password_a ADD COLUMN password_never_expires_m bool;
