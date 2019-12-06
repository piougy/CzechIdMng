--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add ID of role-request to the vs-request

ALTER TABLE vs_request ADD COLUMN role_request_id bytea;
CREATE INDEX idx_vs_request_role_request_id ON vs_request USING btree (role_request_id);


