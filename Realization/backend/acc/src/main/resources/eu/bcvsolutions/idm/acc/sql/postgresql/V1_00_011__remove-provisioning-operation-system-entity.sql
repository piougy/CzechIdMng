--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- remove system entity from provisioning operation - replace with simple uuid, entity type and system
-- add entity type to account

ALTER TABLE sys_provisioning_operation ADD COLUMN entity_type character varying(255);
ALTER TABLE sys_provisioning_operation ADD COLUMN system_entity_uid character varying(255);
ALTER TABLE sys_provisioning_operation ADD COLUMN system_id bytea;

UPDATE sys_provisioning_operation SET entity_type = (SELECT entity_type FROM sys_system_entity WHERE sys_system_entity.id = sys_provisioning_operation.system_entity_id);
UPDATE sys_provisioning_operation SET system_entity_uid = (SELECT uid FROM sys_system_entity WHERE sys_system_entity.id = sys_provisioning_operation.system_entity_id);
UPDATE sys_provisioning_operation SET system_id = (SELECT system_id FROM sys_system_entity WHERE sys_system_entity.id = sys_provisioning_operation.system_entity_id);

CREATE INDEX idx_sys_p_o_entity_type
  ON sys_provisioning_operation
  USING btree
  (entity_type);
  
CREATE INDEX idx_sys_p_o_system
  ON sys_provisioning_operation
  USING btree
  (system_id);

CREATE INDEX idx_sys_p_o_uid
  ON sys_provisioning_operation
  USING btree
  (system_entity_uid);
  
ALTER TABLE sys_provisioning_operation ALTER COLUMN entity_type SET NOT NULL;
ALTER TABLE sys_provisioning_operation ALTER COLUMN system_entity_uid SET NOT NULL;
ALTER TABLE sys_provisioning_operation ALTER COLUMN system_id SET NOT NULL;
  
ALTER TABLE sys_provisioning_operation DROP COLUMN system_entity_id;

-- add entity type to account
ALTER TABLE acc_account ADD COLUMN entity_type character varying(255);
UPDATE acc_account SET entity_type = (SELECT entity_type FROM sys_system_entity WHERE sys_system_entity.id = acc_account.system_entity_id);

