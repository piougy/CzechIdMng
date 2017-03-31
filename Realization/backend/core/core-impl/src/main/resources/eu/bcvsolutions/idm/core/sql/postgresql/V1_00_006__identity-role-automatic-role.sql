--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script update IdmIdentityRole, create new column automatic role

ALTER TABLE idm_identity_role ADD COLUMN automatic_role boolean NOT NULL DEFAULT false;

ALTER TABLE idm_identity_role_a ADD COLUMN automatic_role boolean;

ALTER TABLE idm_identity_role_a ADD COLUMN automatic_role_m boolean;