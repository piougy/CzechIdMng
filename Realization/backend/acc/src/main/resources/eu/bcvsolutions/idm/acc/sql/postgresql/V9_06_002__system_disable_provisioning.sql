--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- New feature - provisioning is disabled on system - just ACM is executed (account uuid is computed).

ALTER TABLE sys_system ADD COLUMN disabled_provisioning bool NOT NULL DEFAULT false;
ALTER TABLE sys_system_a ADD COLUMN disabled_provisioning bool;
ALTER TABLE sys_system_a ADD COLUMN disabled_provisioning_m bool;
