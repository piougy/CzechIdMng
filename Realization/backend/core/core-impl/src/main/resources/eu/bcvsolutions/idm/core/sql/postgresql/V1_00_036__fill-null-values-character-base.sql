--
-- CzechIdM 7.6 Flyway script 
-- BCV solutions s.r.o.
--
-- fill default values for null character bases in password policies 
-- and define not null

UPDATE idm_password_policy SET number_base = '0123456789' WHERE number_base IS NULL OR number_base = '';
UPDATE idm_password_policy SET lower_char_base = 'abcdefghijklmnopqrstuvwxyz' WHERE lower_char_base IS NULL OR lower_char_base = '';
UPDATE idm_password_policy SET upper_char_base = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' WHERE upper_char_base IS NULL OR upper_char_base = '';
UPDATE idm_password_policy SET special_char_base = '!@#$%&*' WHERE special_char_base IS NULL OR special_char_base = '';

ALTER TABLE idm_password_policy ALTER COLUMN number_base SET NOT NULL;
ALTER TABLE idm_password_policy ALTER COLUMN lower_char_base SET NOT NULL;
ALTER TABLE idm_password_policy ALTER COLUMN upper_char_base SET NOT NULL;
ALTER TABLE idm_password_policy ALTER COLUMN special_char_base SET NOT NULL;
