--
-- CzechIdM 7.5 Flyway script 
-- BCV solutions s.r.o.
--
-- add dependent task trigger

-- Table: idm_dependent_task_trigger

-- DROP TABLE idm_dependent_task_trigger;

CREATE TABLE idm_dependent_task_trigger
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
  dependent_task_id character varying(255) NOT NULL,
  initiator_task_id character varying(255) NOT NULL,
  CONSTRAINT idm_dependent_task_trigger_pkey PRIMARY KEY (id)
);

-- Index: idx_idm_dependent_t_dep

CREATE INDEX idx_idm_dependent_t_dep
  ON idm_dependent_task_trigger
  USING btree
  (dependent_task_id);

-- Index: idx_idm_dependent_t_init

CREATE INDEX idx_idm_dependent_t_init
  ON idm_dependent_task_trigger
  USING btree
  (initiator_task_id);

