--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script adds multiple senders for notifications and sms support


----- TABLE idm_long_running_task -----
ALTER TABLE idm_long_running_task ADD COLUMN scheduled_task_id bytea;

CREATE INDEX idx_idm_long_r_t_s_task
  ON idm_long_running_task
  USING btree
  (scheduled_task_id);


-- Table: idm_processed_task_item
CREATE TABLE idm_processed_task_item
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
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  referenced_dto_type character varying(255) NOT NULL,
  referenced_entity_id bytea NOT NULL,
  long_running_task bytea,
  scheduled_task_queue_owner bytea,
  CONSTRAINT idm_processed_task_item_pkey PRIMARY KEY (id)
);

CREATE INDEX idm_processed_t_i_l_r_t
  ON idm_processed_task_item
  USING btree
  (long_running_task);

CREATE INDEX idm_processed_t_i_q_o
  ON idm_processed_task_item
  USING btree
  (scheduled_task_queue_owner);


-- Table: idm_scheduled_task
CREATE TABLE idm_scheduled_task
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
  dry_run boolean,
  quartz_task_name character varying(255) NOT NULL,
  CONSTRAINT idm_scheduled_task_pkey PRIMARY KEY (id),
  CONSTRAINT uk_8bbpr92i3lvuiw52kvmh8ci1c UNIQUE (quartz_task_name)
);


-- Table: idm_scheduled_task_a
CREATE TABLE idm_scheduled_task_a
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
  dry_run boolean,
  dry_run_m boolean,
  quartz_task_name character varying(255),
  quartz_task_name_m boolean,
  CONSTRAINT idm_scheduled_task_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_mdjjd0gqna0tw0ewqf571abj5 FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

