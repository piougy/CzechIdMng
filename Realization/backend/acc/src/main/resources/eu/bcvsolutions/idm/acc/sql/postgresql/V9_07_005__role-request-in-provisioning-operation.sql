--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add ID of role-request to the provisioning operation and archive

ALTER TABLE sys_provisioning_operation ADD COLUMN role_request_id bytea;
CREATE INDEX idx_sys_p_o_role_request_id ON sys_provisioning_operation USING btree (role_request_id);

ALTER TABLE sys_provisioning_archive ADD COLUMN role_request_id bytea;
CREATE INDEX idx_sys_p_a_role_request_id ON sys_provisioning_archive USING btree (role_request_id);

