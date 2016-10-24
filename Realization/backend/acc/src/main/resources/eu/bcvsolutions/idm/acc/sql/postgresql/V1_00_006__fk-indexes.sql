-- add indexes to fk and columns used in search

-- account
CREATE INDEX idx_acc_account_system_id ON acc_account USING btree (system_id);
CREATE INDEX idx_acc_account_system_entity_id ON acc_account USING btree (system_entity_id);
CREATE INDEX idx_acc_account_role_system_id ON acc_account USING btree (role_system_id);

-- identity account
CREATE INDEX idx_acc_identity_account_account_id ON acc_identity_account USING btree (account_id);
CREATE INDEX idx_acc_identity_account_identity_id ON acc_identity_account USING btree (identity_id);
CREATE INDEX idx_acc_identity_identity_role_id ON acc_identity_account USING btree (identity_role_id);

-- system entity
CREATE INDEX idx_sys_system_entity_system_id ON sys_system_entity USING btree (system_id);

-- role system
CREATE INDEX idx_sys_role_system_system_id ON sys_role_system USING btree (system_id);
CREATE INDEX idx_sys_role_system_role_id ON sys_role_system USING btree (role_id);