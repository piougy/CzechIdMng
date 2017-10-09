--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- Add entity for relation between IdmIdentityContract and AccAccount

-- Table: acc_contract_account
CREATE TABLE acc_contract_account
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
  ownership boolean NOT NULL,
  account_id bytea NOT NULL,
  contract_id bytea NOT NULL,
  CONSTRAINT acc_contract_account_pkey PRIMARY KEY (id)
);


-- Index: idx_acc_contr_acc_acc
CREATE INDEX idx_acc_contr_acc_acc
  ON acc_contract_account
  USING btree
  (account_id);

-- Index: idx_acc_contr_acc_contr
CREATE INDEX idx_acc_contr_acc_contr
  ON acc_contract_account
  USING btree
  (contract_id);
  

 -- Table: acc_contract_account_a
CREATE TABLE acc_contract_account_a
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
  ownership boolean,
  ownership_m boolean,
  account_id bytea,
  account_m boolean,
  contract_id bytea,
  contract_m boolean,
  CONSTRAINT acc_contract_account_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_yrumm9gw9dr2xbrbll5m3bd3 FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);



