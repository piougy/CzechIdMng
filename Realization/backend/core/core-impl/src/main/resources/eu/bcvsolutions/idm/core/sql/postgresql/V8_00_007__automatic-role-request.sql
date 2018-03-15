--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Automatic role request


-- Table: idm_auto_role_request

CREATE TABLE idm_auto_role_request
(
  id bytea NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  creator_id bytea,
  modified timestamp without time zone,
  modifier character varying(255),
  modifier_id bytea,
  original_creator character varying(255),
  original_creator_id bytea,
  original_modifier character varying(255),
  original_modifier_id bytea,
  realm_id bytea,
  transaction_id bytea,
  description character varying(2000),
  execute_immediately boolean NOT NULL,
  name character varying(255),
  operation character varying(255),
  recursion_type character varying(255),
  request_type character varying(255) NOT NULL,
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  state character varying(255) NOT NULL,
  wf_process_id character varying(255),
  auto_role_att_id bytea,
  role_id bytea,
  tree_node_id bytea,
  CONSTRAINT idm_auto_role_request_pkey PRIMARY KEY (id)
);

-- Index: idx_idm_auto_role_name_req

CREATE INDEX idx_idm_auto_role_name_req
  ON idm_auto_role_request
  USING btree
  (name);

-- Index: idx_idm_auto_role_role_req

CREATE INDEX idx_idm_auto_role_role_req
  ON idm_auto_role_request
  USING btree
  (role_id);
  

-- Table: idm_auto_role_request_a
  
  CREATE TABLE idm_auto_role_request_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  created_m boolean,
  creator character varying(255),
  creator_m boolean,
  creator_id bytea,
  creator_id_m boolean,
  modified timestamp without time zone,
  modified_m boolean,
  modifier character varying(255),
  modifier_m boolean,
  modifier_id bytea,
  modifier_id_m boolean,
  original_creator character varying(255),
  original_creator_m boolean,
  original_creator_id bytea,
  original_creator_id_m boolean,
  original_modifier character varying(255),
  original_modifier_m boolean,
  original_modifier_id bytea,
  original_modifier_id_m boolean,
  realm_id bytea,
  realm_id_m boolean,
  transaction_id bytea,
  transaction_id_m boolean,
  description character varying(2000),
  description_m boolean,
  execute_immediately boolean,
  execute_immediately_m boolean,
  name character varying(255),
  name_m boolean,
  operation character varying(255),
  operation_m boolean,
  recursion_type character varying(255),
  recursion_type_m boolean,
  request_type character varying(255),
  request_type_m boolean,
  state character varying(255),
  state_m boolean,
  wf_process_id character varying(255),
  wf_process_id_m boolean,
  auto_role_att_id bytea,
  automatic_role_m boolean,
  role_id bytea,
  role_m boolean,
  tree_node_id bytea,
  tree_node_m boolean,
  CONSTRAINT idm_auto_role_request_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_3c5uwgj4whal2entaae9xsros FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Table: idm_auto_role_att_rule_req

CREATE TABLE idm_auto_role_att_rule_req
(
  id bytea NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  creator_id bytea,
  modified timestamp without time zone,
  modifier character varying(255),
  modifier_id bytea,
  original_creator character varying(255),
  original_creator_id bytea,
  original_modifier character varying(255),
  original_modifier_id bytea,
  realm_id bytea,
  transaction_id bytea,
  attribute_name character varying(255),
  comparison character varying(255),
  operation character varying(255) NOT NULL,
  type character varying(255),
  value character varying(2000),
  form_attribute_id bytea,
  auto_role_att_id bytea NOT NULL,
  rule_id bytea,
  CONSTRAINT idm_auto_role_att_rule_req_pkey PRIMARY KEY (id)
);

-- Index: idx_idm_au_r_att_rule_id_req

CREATE INDEX idx_idm_au_r_att_rule_id_req
  ON idm_auto_role_att_rule_req
  USING btree
  (auto_role_att_id);

-- Index: idx_idm_au_r_att_rule_req_rule

CREATE INDEX idx_idm_au_r_att_rule_req_rule
  ON idm_auto_role_att_rule_req
  USING btree
  (rule_id);

-- Index: idx_idm_au_r_form_att_id_req

CREATE INDEX idx_idm_au_r_form_att_id_req
  ON idm_auto_role_att_rule_req
  USING btree
  (form_attribute_id);

-- Index: idx_idm_au_r_form_att_n_req

CREATE INDEX idx_idm_au_r_form_att_n_req
  ON idm_auto_role_att_rule_req
  USING btree
  (attribute_name);

-- Index: idx_idm_au_r_form_type_req

CREATE INDEX idx_idm_au_r_form_type_req
  ON idm_auto_role_att_rule_req
  USING btree
  (type);
  
  -- Table: idm_auto_role_att_rule_req_a

CREATE TABLE idm_auto_role_att_rule_req_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  created_m boolean,
  creator character varying(255),
  creator_m boolean,
  creator_id bytea,
  creator_id_m boolean,
  modified timestamp without time zone,
  modified_m boolean,
  modifier character varying(255),
  modifier_m boolean,
  modifier_id bytea,
  modifier_id_m boolean,
  original_creator character varying(255),
  original_creator_m boolean,
  original_creator_id bytea,
  original_creator_id_m boolean,
  original_modifier character varying(255),
  original_modifier_m boolean,
  original_modifier_id bytea,
  original_modifier_id_m boolean,
  realm_id bytea,
  realm_id_m boolean,
  transaction_id bytea,
  transaction_id_m boolean,
  attribute_name character varying(255),
  attribute_name_m boolean,
  comparison character varying(255),
  comparison_m boolean,
  operation character varying(255),
  operation_m boolean,
  type character varying(255),
  type_m boolean,
  value character varying(2000),
  value_m boolean,
  form_attribute_id bytea,
  form_attribute_m boolean,
  auto_role_att_id bytea,
  request_m boolean,
  rule_id bytea,
  rule_m boolean,
  CONSTRAINT idm_auto_role_att_rule_req_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_h5wcaotvqs4m7pf0siqfr27u4 FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);





