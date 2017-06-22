--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- Add indexes for owner and sub owner column

CREATE INDEX idx_idm_audit_owner_id
  ON idm_audit
  USING btree
  (owner_id);
  
CREATE INDEX idx_idm_audit_owner_code
  ON idm_audit
  USING btree
  (owner_code);

CREATE INDEX idx_idm_audit_owner_type
  ON idm_audit
  USING btree
  (owner_type);

CREATE INDEX idx_idm_audit_sub_owner_id
  ON idm_audit
  USING btree
  (sub_owner_id);

CREATE INDEX idx_idm_audit_sub_owner_code
  ON idm_audit
  USING btree
  (sub_owner_code);

CREATE INDEX idx_idm_audit_sub_owner_type
  ON idm_audit
  USING btree
  (sub_owner_type);
