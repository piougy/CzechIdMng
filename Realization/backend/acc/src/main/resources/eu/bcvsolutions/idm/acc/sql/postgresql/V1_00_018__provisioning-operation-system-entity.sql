--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Add new column to provisioning operation "system_entity_id" keep relation to the system entity.
-- Colum "system_entity_uid" contains UID in string will be deleted.

ALTER TABLE sys_provisioning_operation
  ADD COLUMN system_entity_id bytea;

-- Transform uid in string to the system entity ID.
UPDATE sys_provisioning_operation spo 
  SET system_entity_id = 
   (SELECT se.id FROM sys_system_entity se WHERE se.system_id = spo.system_id AND spo.system_entity_uid = se.uid);

-- Delete unsuccessful tranformed data
DELETE FROM sys_provisioning_operation po WHERE po.system_entity_id is null; 

ALTER TABLE sys_provisioning_operation 
  ALTER COLUMN system_entity_id SET NOT NULL;

-- Delete old column with UID in string
ALTER TABLE sys_provisioning_operation
 DROP COLUMN system_entity_uid;

CREATE INDEX idx_sys_p_o_sys_entity
  ON sys_provisioning_operation
  USING btree
  (system_entity_id); 
