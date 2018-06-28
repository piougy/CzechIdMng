--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Forward account management


ALTER TABLE sys_role_system ADD COLUMN forward_acm_enabled boolean;
ALTER TABLE sys_role_system_a ADD COLUMN forward_acm_enabled boolean;
ALTER TABLE sys_role_system_a ADD COLUMN forward_account_managemen_m boolean;