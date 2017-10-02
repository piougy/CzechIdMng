--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- add new face type attribut and migrate persistent types

ALTER TABLE idm_form_attribute ADD COLUMN face_type character varying(45);
ALTER TABLE idm_form_attribute_a ADD COLUMN face_type character varying(45);
ALTER TABLE idm_form_attribute_a ADD COLUMN face_type_m boolean;

-- clean up persistent type 
update idm_form_attribute set face_type = persistent_type;
update idm_form_attribute set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_form_attribute set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
update idm_form_attribute_a set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_form_attribute_a set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
--
update idm_identity_form_value set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_identity_form_value set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
update idm_identity_form_value_a set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_identity_form_value_a set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
--
update idm_role_form_value set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_role_form_value set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
update idm_role_form_value_a set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_role_form_value_a set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
--
update idm_i_contract_form_value set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_i_contract_form_value set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
update idm_i_contract_form_value_a set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_i_contract_form_value_a set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
--
update idm_tree_node_form_value set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_tree_node_form_value set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
update idm_tree_node_form_value_a set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update idm_tree_node_form_value_a set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
--
-- add uuid persistent type
ALTER TABLE idm_identity_form_value ADD COLUMN uuid_value bytea;
ALTER TABLE idm_identity_form_value_a ADD COLUMN uuid_value bytea;
ALTER TABLE idm_identity_form_value_a ADD COLUMN uuid_value_m boolean;
--
ALTER TABLE idm_role_form_value ADD COLUMN uuid_value bytea;
ALTER TABLE idm_role_form_value_a ADD COLUMN uuid_value bytea;
ALTER TABLE idm_role_form_value_a ADD COLUMN uuid_value_m boolean;
--
ALTER TABLE idm_i_contract_form_value ADD COLUMN uuid_value bytea;
ALTER TABLE idm_i_contract_form_value_a ADD COLUMN uuid_value bytea;
ALTER TABLE idm_i_contract_form_value_a ADD COLUMN uuid_value_m boolean;
--
ALTER TABLE idm_tree_node_form_value ADD COLUMN uuid_value bytea;
ALTER TABLE idm_tree_node_form_value_a ADD COLUMN uuid_value bytea;
ALTER TABLE idm_tree_node_form_value_a ADD COLUMN uuid_value_m boolean;


