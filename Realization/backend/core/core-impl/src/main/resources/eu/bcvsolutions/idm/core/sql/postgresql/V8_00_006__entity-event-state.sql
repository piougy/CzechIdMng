--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- persist entity events for asynchronous event processing

CREATE TABLE idm_entity_event
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
  closed boolean NOT NULL,
  content bytea,
  event_type character varying(255),
  execute_date timestamp without time zone,
  instance_id character varying(255) NOT NULL,
  original_source bytea,
  owner_id bytea NOT NULL,
  owner_type character varying(255) NOT NULL,
  parent_event_type character varying(255),
  priority character varying(45) NOT NULL,
  processed_order integer,
  properties bytea,
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  suspended boolean NOT NULL,
  parent_id bytea,
  CONSTRAINT idm_entity_event_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_entity_event_created
  ON idm_entity_event
  USING btree
  (created);

CREATE INDEX idx_idm_entity_event_exe
  ON idm_entity_event
  USING btree
  (execute_date);

CREATE INDEX idx_idm_entity_event_inst
  ON idm_entity_event
  USING btree
  (instance_id);

CREATE INDEX idx_idm_entity_event_o_id
  ON idm_entity_event
  USING btree
  (owner_id);

CREATE INDEX idx_idm_entity_event_o_type
  ON idm_entity_event
  USING btree
  (owner_type);
  
  
CREATE TABLE idm_entity_state
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
  closed boolean NOT NULL,
  instance_id character varying(255) NOT NULL,
  owner_id bytea NOT NULL,
  owner_type character varying(255) NOT NULL,
  processed_order integer,
  processor_id character varying(255),
  processor_module character varying(255),
  processor_name character varying(255),
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  suspended boolean NOT NULL,
  event_id bytea,
  CONSTRAINT idm_entity_state_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_entity_state_event
  ON idm_entity_state
  USING btree
  (event_id);

CREATE INDEX idx_idm_entity_state_o_id
  ON idm_entity_state
  USING btree
  (owner_id);

CREATE INDEX idx_idm_entity_state_o_type
  ON idm_entity_state
  USING btree
  (owner_type);
