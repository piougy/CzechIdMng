-- role-system module prefix change

ALTER TABLE acc_role_system RENAME TO sys_role_system;
ALTER TABLE acc_role_system_aud RENAME TO sys_role_system_aud;

-- account based on role-system template

ALTER TABLE acc_account ADD COLUMN role_system_id bigint;
ALTER TABLE acc_account_aud ADD COLUMN role_system_id bigint;

ALTER TABLE ONLY acc_account
    ADD CONSTRAINT fk_acc_account_role_system_id FOREIGN KEY (role_system_id) REFERENCES sys_role_system(id) ON DELETE SET NULL;



