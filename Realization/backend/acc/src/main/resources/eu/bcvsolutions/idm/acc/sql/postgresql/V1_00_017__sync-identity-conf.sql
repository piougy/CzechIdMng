--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Set default values for blocked operations on system,


-- Table: sys_sync_identity_config

CREATE TABLE sys_sync_identity_config
(
  id bytea NOT NULL,
  default_role_id bytea,
  CONSTRAINT sys_sync_identity_config_pkey PRIMARY KEY (id),
  CONSTRAINT fk_mf7itnp88831l71bl3tkgcqpo FOREIGN KEY (id)
      REFERENCES sys_sync_config (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Table: sys_sync_identity_config_a

CREATE TABLE sys_sync_identity_config_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  default_role_id bytea,
  default_role_m boolean,
  CONSTRAINT sys_sync_identity_config_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_jc3sotalvrdqvt6xqh8msx9t3 FOREIGN KEY (id, rev)
      REFERENCES sys_sync_config_a (id, rev) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- We have to insert data from abstract sync conf (for identity sync) to the new sys_sync_identity_config

INSERT INTO sys_sync_identity_config (
  SELECT c.id FROM sys_sync_config c WHERE c.id NOT IN (
    SELECT conf.id FROM sys_sync_config conf JOIN sys_sync_contract_config cont ON(conf.id = cont.id)
  )
)