-- when role is removed, when assigned relation with system are deleted too
ALTER TABLE ONLY sys_role_system DROP CONSTRAINT fk_acc_role_system_role_id;
ALTER TABLE ONLY sys_role_system ADD CONSTRAINT fk_acc_role_system_role_id FOREIGN KEY (role_id) REFERENCES idm_role(id) ON DELETE CASCADE;