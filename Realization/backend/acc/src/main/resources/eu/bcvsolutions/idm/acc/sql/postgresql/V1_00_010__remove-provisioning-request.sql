--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- remove provisioning operation request table - move columns to provisioning operation

ALTER TABLE sys_provisioning_operation ADD COLUMN current_attempt integer;
ALTER TABLE sys_provisioning_operation ADD COLUMN max_attempts integer;
ALTER TABLE sys_provisioning_operation ADD COLUMN result_cause text;
ALTER TABLE sys_provisioning_operation ADD COLUMN result_code character varying(255);
ALTER TABLE sys_provisioning_operation ADD COLUMN result_model bytea;
ALTER TABLE sys_provisioning_operation ADD COLUMN result_state character varying(45);
ALTER TABLE sys_provisioning_operation ADD COLUMN provisioning_batch_id bytea;

CREATE INDEX idx_sys_pro_oper_batch_id
  ON sys_provisioning_operation
  USING btree
  (provisioning_batch_id);
  
UPDATE sys_provisioning_operation SET current_attempt = (SELECT current_attempt FROM sys_provisioning_request WHERE provisioning_operation_id = sys_provisioning_operation.id);
UPDATE sys_provisioning_operation SET max_attempts = (SELECT max_attempts FROM sys_provisioning_request WHERE provisioning_operation_id = sys_provisioning_operation.id);
UPDATE sys_provisioning_operation SET result_cause = (SELECT result_cause FROM sys_provisioning_request WHERE provisioning_operation_id = sys_provisioning_operation.id);
UPDATE sys_provisioning_operation SET result_code = (SELECT result_code FROM sys_provisioning_request WHERE provisioning_operation_id = sys_provisioning_operation.id);
UPDATE sys_provisioning_operation SET result_model = (SELECT result_model FROM sys_provisioning_request WHERE provisioning_operation_id = sys_provisioning_operation.id);
UPDATE sys_provisioning_operation SET result_state = (SELECT result_state FROM sys_provisioning_request WHERE provisioning_operation_id = sys_provisioning_operation.id);
UPDATE sys_provisioning_operation SET provisioning_batch_id = (SELECT provisioning_batch_id FROM sys_provisioning_request WHERE provisioning_operation_id = sys_provisioning_operation.id);

DROP TABLE sys_provisioning_request;


