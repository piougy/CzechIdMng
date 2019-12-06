--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add ID of role-request to the provisioning operation and archive

ALTER TABLE sys_provisioning_operation ADD role_request_id binary(16);
CREATE INDEX idx_sys_p_o_role_request_id ON sys_provisioning_operation (role_request_id);

ALTER TABLE sys_provisioning_archive ADD role_request_id binary(16);
CREATE INDEX idx_sys_p_a_role_request_id ON sys_provisioning_archive (role_request_id);
