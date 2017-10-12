--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- Add entity for contract sync

-- Table: sys_sync_contract_config
CREATE TABLE sys_sync_contract_config
(
  id bytea NOT NULL,
  default_leader_id bytea,
  default_tree_node_id bytea,
  default_tree_type_id bytea,
  CONSTRAINT sys_sync_contract_config_pkey PRIMARY KEY (id),
  CONSTRAINT fk_bc6q115e4e8r9dautiucnweyi FOREIGN KEY (id)
      REFERENCES sys_sync_config (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Table: sys_sync_contract_config_a
CREATE TABLE sys_sync_contract_config_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  default_leader_id bytea,
  default_leader_m boolean,
  default_tree_node_id bytea,
  default_tree_node_m boolean,
  default_tree_type_id bytea,
  default_tree_type_m boolean,
  CONSTRAINT sys_sync_contract_config_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_ak23lsavvslhvywtevxaq4m2u FOREIGN KEY (id, rev)
      REFERENCES sys_sync_config_a (id, rev) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
