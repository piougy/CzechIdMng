--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM 7.0 - Module Report


----- TABLE rpt_report -----
CREATE TABLE rpt_report
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
  name character varying(255) NOT NULL,
  executor_name character varying(255) NOT NULL,
  data_id bytea,
  long_running_task_id bytea,
  CONSTRAINT rpt_report_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_rpt_report_name
  ON rpt_report
  USING btree
  (name);
  
CREATE INDEX idx_rpt_report_executor
  ON rpt_report
  USING btree
  (executor_name);
  
CREATE INDEX idx_rpt_report_lrt_id
  ON rpt_report
  USING btree
  (long_running_task_id);
  
  
----- TABLE rpt_report_a -----
CREATE TABLE rpt_report_a
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
  name character varying(255),
  name_m boolean,
  executor_name character varying(255),
  executor_name_m boolean,
  data_id bytea,
  data_m boolean,
  long_running_task_id bytea,
  long_running_task_m boolean,
  CONSTRAINT idm_rpt_report_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_rpt_report_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
