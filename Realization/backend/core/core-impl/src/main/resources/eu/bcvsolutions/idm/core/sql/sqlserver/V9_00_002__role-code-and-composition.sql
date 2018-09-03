--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add composition index
--
ALTER TABLE idm_role_composition ADD CONSTRAINT ux_idm_role_composition_susu UNIQUE (superior_id,sub_id);

-- role code added in int script


