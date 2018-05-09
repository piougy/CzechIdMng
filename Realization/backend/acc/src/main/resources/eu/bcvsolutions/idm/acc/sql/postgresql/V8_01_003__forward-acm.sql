--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Forward account management


update sys_role_system set forward_acm_enabled = false;
alter table sys_role_system alter column forward_acm_enabled set not null; 
