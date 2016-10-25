-- rename fk constraints,
-- add indexes to fk and columns used in search

-- identity
ALTER TABLE ONLY idm_identity DROP CONSTRAINT uk_5aqknnxk13ycwr46tpbl6j1tt;
ALTER TABLE ONLY idm_identity ADD CONSTRAINT ux_idm_identity_username UNIQUE (username); 

-- identity contract
ALTER TABLE ONLY idm_identity_contract DROP CONSTRAINT fk_bokg2ecvedrjayicxk5uapsm3;
ALTER TABLE ONLY idm_identity_contract ADD CONSTRAINT fk_idm_identity_contract_guarantee FOREIGN KEY (guarantee_id) REFERENCES idm_identity(id);
ALTER TABLE ONLY idm_identity_contract DROP CONSTRAINT fk_tg0elpmq774jvn1dihfht4021;
ALTER TABLE ONLY idm_identity_contract ADD CONSTRAINT fk_idm_identity_contract_identity FOREIGN KEY (identity_id) REFERENCES idm_identity(id)  ON DELETE CASCADE;;
CREATE INDEX idx_idm_identity_contract_guarantee ON idm_identity_contract USING btree (guarantee_id);
CREATE INDEX idx_idm_identity_contract_identity ON idm_identity_contract USING btree (identity_id);
CREATE INDEX idx_idm_identity_contract_working_position ON idm_identity_contract USING btree (working_position_id);

-- identity role
ALTER TABLE ONLY idm_identity_role DROP CONSTRAINT fk_grl8dgoxq29agvhwx9ycvq99y;
ALTER TABLE ONLY idm_identity_role ADD CONSTRAINT fk_idm_identity_role_identity FOREIGN KEY (identity_id) REFERENCES idm_identity(id) ON DELETE CASCADE;
ALTER TABLE ONLY idm_identity_role DROP CONSTRAINT fk_hh6a5m1l5rkj52mrps4w1a4ti;
ALTER TABLE ONLY idm_identity_role ADD CONSTRAINT fk_idm_identity_role_role FOREIGN KEY (role_id) REFERENCES idm_role(id);
CREATE INDEX idx_idm_identity_role_identity ON idm_identity_role USING btree (identity_id);
CREATE INDEX idx_idm_identity_role_role ON idm_identity_role USING btree (role_id);

-- role
DROP INDEX ux_role_name; -- remove duplicit index
ALTER TABLE ONLY idm_role DROP CONSTRAINT uk_rf8ejw5hpvy7eewpnijmoeh32;
ALTER TABLE ONLY idm_role ADD CONSTRAINT ux_idm_role_name UNIQUE (name); -- this will be removed anyway in future (technical role name has to be added) 

-- role authority
ALTER TABLE ONLY idm_role_authority DROP CONSTRAINT fk_p5jsvce9inw4u2wsji6k13r3q;
ALTER TABLE ONLY idm_role_authority ADD CONSTRAINT fk_idm_role_authority_role FOREIGN KEY (role_id) REFERENCES idm_role(id) ON DELETE CASCADE;
CREATE INDEX idx_idm_role_authority_role ON idm_role_authority USING btree (role_id);

-- role composition
ALTER TABLE ONLY idm_role_composition DROP CONSTRAINT fk_4ug07v1wi3g1ai54k8uj0qekb;
ALTER TABLE ONLY idm_role_composition ADD CONSTRAINT fk_idm_role_composition_sub FOREIGN KEY (sub_id) REFERENCES idm_role(id) ON DELETE CASCADE;
ALTER TABLE ONLY idm_role_composition DROP CONSTRAINT fk_bokxu1laccj6t8f4ddduy3ud8;
ALTER TABLE ONLY idm_role_composition ADD CONSTRAINT fk_idm_role_composition_superior FOREIGN KEY (superior_id) REFERENCES idm_role(id) ON DELETE CASCADE;
CREATE INDEX idx_idm_role_composition_sub ON idm_role_composition USING btree (sub_id);
CREATE INDEX idx_idm_role_composition_superior ON idm_role_composition USING btree (superior_id);

-- role guarantee
CREATE INDEX idx_idm_role_guarantee_guarantee ON idm_role_guarantee USING btree (guarantee_id);
CREATE INDEX idx_idm_role_guarantee_role ON idm_role_guarantee USING btree (role_id);

-- tree node
CREATE INDEX idx_idm_tree_node_parent ON idm_tree_node USING btree (parent_id);
CREATE INDEX idx_idm_tree_node_type ON idm_tree_node USING btree (tree_type_id);

-- email log
ALTER TABLE ONLY idm_email_log DROP CONSTRAINT fk_mhyevbh5gbrwiw2geq69empko;
ALTER TABLE ONLY idm_email_log ADD CONSTRAINT fk_idm_email_log_notification FOREIGN KEY (id) REFERENCES idm_notification(id);

-- notification
ALTER TABLE ONLY idm_notification DROP CONSTRAINT fk_8gtuvvraara46uuw7y40ddm2f;
ALTER TABLE ONLY idm_notification ADD CONSTRAINT fk_idm_notification_sender FOREIGN KEY (sender_id) REFERENCES idm_identity(id) ON DELETE SET NULL;
ALTER TABLE ONLY idm_notification DROP CONSTRAINT fk_md4u8k85gjsm7t853ro49474i;
ALTER TABLE ONLY idm_notification ADD CONSTRAINT fk_idm_notification_parent FOREIGN KEY (parent_notification_id) REFERENCES idm_notification(id) ON DELETE CASCADE;
CREATE INDEX idx_idm_notification_sender ON idm_notification USING btree (sender_id);
CREATE INDEX idx_idm_notification_parent ON idm_notification USING btree (parent_notification_id);

-- notification log
ALTER TABLE ONLY idm_notification_log DROP CONSTRAINT fk_6lxo8e33m2cn2kemxjfo72cp7;
ALTER TABLE ONLY idm_notification_log ADD CONSTRAINT fk_idm_notification_log_notification FOREIGN KEY (id) REFERENCES idm_notification(id);





