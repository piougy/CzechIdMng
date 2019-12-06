--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- New feature - provisioning is disabled on system - just ACM is executed (account uuid is computed).

ALTER TABLE sys_system ADD disabled_provisioning bit NOT NULL DEFAULT 0;
ALTER TABLE sys_system_a ADD disabled_provisioning bit;
ALTER TABLE sys_system_a ADD disabled_provisioning_m bit;
