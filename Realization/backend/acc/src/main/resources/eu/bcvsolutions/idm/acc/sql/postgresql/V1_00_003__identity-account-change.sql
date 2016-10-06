-- identity account should be linked to identity role (accontt was based on identity role)

ALTER TABLE ONLY acc_identity_account DROP CONSTRAINT fk_acc_identity_account_role_id;

UPDATE acc_identity_account SET role_id = null;
ALTER TABLE acc_identity_account RENAME COLUMN role_id TO identity_role_id;

ALTER TABLE ONLY acc_identity_account
    ADD CONSTRAINT fk_acc_identity_account_role_id FOREIGN KEY (identity_role_id) REFERENCES idm_identity_role(id);

ALTER TABLE acc_identity_account_aud DROP COLUMN role_id;
ALTER TABLE acc_identity_account_aud ADD COLUMN identity_role_id bigint;

-- virtual system preparation

ALTER TABLE sys_system ADD COLUMN virtual boolean DEFAULT FALSE;
ALTER TABLE sys_system_aud ADD COLUMN virtual boolean DEFAULT FALSE;

