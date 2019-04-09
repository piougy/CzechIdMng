--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- New feature password never expires

ALTER TABLE idm_password ADD password_never_expires bit NOT NULL DEFAULT 0;
ALTER TABLE idm_password_a ADD password_never_expires bit;
ALTER TABLE idm_password_a ADD password_never_expires_m bit
