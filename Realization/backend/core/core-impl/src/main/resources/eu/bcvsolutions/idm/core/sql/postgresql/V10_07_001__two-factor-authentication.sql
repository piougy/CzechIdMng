--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- support two factor authentication

-- secret code (Base32 currently used)
ALTER TABLE idm_password ADD COLUMN verification_secret varchar(255);
-- token is verified
ALTER TABLE idm_token ADD COLUMN secret_verified boolean NOT NULL DEFAULT true;
-- profile - add two factor authentication method
ALTER TABLE idm_profile ADD COLUMN two_factor_authentication_type varchar(45);
ALTER TABLE idm_profile_a ADD COLUMN two_factor_authentication_type varchar(45);
ALTER TABLE idm_profile_a ADD COLUMN two_factor_authentication_type_m bool;