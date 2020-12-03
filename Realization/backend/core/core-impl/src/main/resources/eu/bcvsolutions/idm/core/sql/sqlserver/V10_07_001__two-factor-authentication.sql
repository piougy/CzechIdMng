--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- support two factor authentication

-- secret code (Base32 currently used)
ALTER TABLE idm_password ADD verification_secret nvarchar(255);
-- token is verified
ALTER TABLE idm_token ADD secret_verified bit NOT NULL DEFAULT 1;
-- profile - add two factor authentication method
ALTER TABLE idm_profile ADD two_factor_authentication_type nvarchar(45);
ALTER TABLE idm_profile_a ADD two_factor_authentication_type nvarchar(45);
ALTER TABLE idm_profile_a ADD two_factor_authentication_type_m bit;