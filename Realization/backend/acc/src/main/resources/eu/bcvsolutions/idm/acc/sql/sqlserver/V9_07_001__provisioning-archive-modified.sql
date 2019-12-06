--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Provisioning archive - preserve original provisioning request created date

UPDATE sys_provisioning_archive SET modified = created WHERE modified IS NULL;