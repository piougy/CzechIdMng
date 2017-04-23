--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Add more guarantees to identity contract (remove previous guarantees)

CREATE TABLE idm_contract_guarantee
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
  guarantee_id bytea NOT NULL,
  identity_contract_id bytea NOT NULL,
  CONSTRAINT idm_contract_guarantee_pkey PRIMARY KEY (id)
);

CREATE INDEX idm_contract_guarantee_contr
  ON idm_contract_guarantee
  USING btree
  (identity_contract_id);

CREATE INDEX idx_contract_guarantee_idnt
  ON idm_contract_guarantee
  USING btree
  (guarantee_id);

CREATE TABLE idm_contract_guarantee_a
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
  guarantee_id bytea,
  guarantee_m boolean,
  identity_contract_id bytea,
  identity_contract_m boolean,
  CONSTRAINT idm_contract_guarantee_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_dglppsjqr3kdnqtdbfipbtma5 FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- remove previous guarantee from contract table
ALTER TABLE idm_identity_contract DROP COLUMN guarantee_id;
ALTER TABLE idm_identity_contract_a DROP COLUMN guarantee_id;
