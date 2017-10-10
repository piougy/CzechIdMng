--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- add indexs

CREATE INDEX idx_vs_request_system
  ON vs_request
  USING btree
  (system_id);
  

CREATE INDEX idx_vs_request_uid
  ON vs_request
  USING btree
  (uid);
  
CREATE INDEX idx_vs_account_system
  ON vs_account
  USING btree
  (system_id);
  
CREATE INDEX idx_vs_account_uid
  ON vs_account
  USING btree
  (uid);
  
CREATE INDEX idx_vs_sys_imple_identity
  ON vs_system_implementer
  USING btree
  (identity_id);
  
CREATE INDEX idx_vs_sys_imple_role
  ON vs_system_implementer
  USING btree
  (role_id);
  
CREATE INDEX idx_vs_sys_imple_system
  ON vs_system_implementer
  USING btree
  (system_id);

