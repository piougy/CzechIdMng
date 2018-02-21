--
-- CzechIdM 8.0.0 Flyway script 
-- BCV solutions s.r.o.
--
-- add indexes for role request and envers table revchanges

CREATE INDEX idx_idm_role_request_state
  ON idm_role_request
  USING btree
  (state);

CREATE INDEX idx_idm_role_request_app_id
  ON idm_role_request
  USING btree
  (applicant_id);

CREATE INDEX idx_revchanges
  ON revchanges
  USING btree
  (rev);

ALTER TABLE idm_audit ALTER COLUMN changed_attributes TYPE varchar(2000);
  