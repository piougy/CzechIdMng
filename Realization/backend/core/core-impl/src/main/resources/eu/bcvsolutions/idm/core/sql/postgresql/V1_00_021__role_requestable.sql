--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Drop audit for role authorities
ALTER TABLE idm_role ADD COLUMN can_be_requested boolean NOT NULL DEFAULT TRUE ;
ALTER TABLE idm_role_a ADD COLUMN can_be_requested boolean NOT NULL DEFAULT TRUE ;
ALTER TABLE idm_role_a ADD COLUMN can_be_requested_m boolean NOT NULL DEFAULT TRUE ;