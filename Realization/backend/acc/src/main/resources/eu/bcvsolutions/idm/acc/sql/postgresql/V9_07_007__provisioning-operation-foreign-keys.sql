--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add foreign keys to provisioning operation table

-- provisioning operation
ALTER TABLE sys_provisioning_operation ADD CONSTRAINT fk_k1ad29ndiwuldqem93a01c6qx FOREIGN KEY (system_entity_id) REFERENCES sys_system_entity(id);
ALTER TABLE sys_provisioning_operation ADD CONSTRAINT fk_7vyyfd4iyidh411rqdoa3086b FOREIGN KEY (provisioning_batch_id) REFERENCES sys_provisioning_batch(id);
ALTER TABLE sys_provisioning_operation ADD CONSTRAINT fk_reb3mqn6a4f5mxdeipcrhthw9 FOREIGN KEY (system_id) REFERENCES sys_system(id);