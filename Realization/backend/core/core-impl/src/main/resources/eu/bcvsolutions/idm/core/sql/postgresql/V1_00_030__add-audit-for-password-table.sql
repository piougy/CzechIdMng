--
-- CzechIdM 7.4 Flyway script 
-- BCV solutions s.r.o.
--
-- add audit table for password table

CREATE TABLE idm_password_a
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
  must_change boolean,
  must_change_m boolean,
  valid_from date,
  valid_from_m boolean,
  valid_till date,
  valid_till_m boolean,
  identity_id bytea,
  identity_m boolean,
  CONSTRAINT idm_password_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_1t5dpjknl7r4lxhfauqu1h2tm FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
