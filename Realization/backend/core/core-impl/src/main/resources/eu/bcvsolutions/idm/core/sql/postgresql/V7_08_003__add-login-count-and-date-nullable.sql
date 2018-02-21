--
-- CzechIdM 8.0.0 Flyway script 
-- BCV solutions s.r.o.
--
-- last login and unsuccessful login attempts - audit columns can be not null

ALTER TABLE idm_password_a ALTER COLUMN unsuccessful_attempts DROP NOT NULL;

  