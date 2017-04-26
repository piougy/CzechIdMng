--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Remove script name constraint for unique

alter table idm_script drop constraint ux_script_name;

alter table idm_script alter column name drop not null;
