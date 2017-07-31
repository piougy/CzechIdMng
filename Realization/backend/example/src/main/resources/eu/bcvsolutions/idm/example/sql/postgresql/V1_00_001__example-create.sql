--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module example)

CREATE TABLE example_product
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
  code character varying(255) NOT NULL,
  description character varying(2000),
  disabled boolean NOT NULL,
  name character varying(255) NOT NULL,
  double_value numeric(38,4),
  CONSTRAINT example_product_pkey PRIMARY KEY (id),
  CONSTRAINT ux_example_product_code UNIQUE (code)
);

CREATE INDEX idx_example_product_name
  ON example_product
  USING btree
  (name);

-- example product audit
CREATE TABLE example_product_a
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
  code character varying(255),
  code_m boolean,
  description character varying(2000),
  description_m boolean,
  disabled boolean,
  disabled_m boolean,
  name character varying(255),
  name_m boolean,
  double_value numeric(38,4),
  price_m boolean,
  CONSTRAINT example_product_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_l8tlj6h1v48rav5nulvesvbg1 FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

