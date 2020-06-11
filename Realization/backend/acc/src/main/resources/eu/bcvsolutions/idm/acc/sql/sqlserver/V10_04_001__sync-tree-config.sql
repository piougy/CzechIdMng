--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Tree synchronization config

CREATE TABLE sys_sync_tree_config (
	start_auto_role_rec bit NOT NULL,
	id binary(16) NOT NULL,
	CONSTRAINT sys_sync_tree_config_pkey PRIMARY KEY (id),
	CONSTRAINT fk_5pwoxi6mopl3yr5q5ml1j6h9i FOREIGN KEY (id) REFERENCES sys_sync_config(id)
);

CREATE TABLE sys_sync_tree_config_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	start_auto_role_rec bit,
	start_auto_role_rec_m bit,
	CONSTRAINT sys_sync_tree_config_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_ak21lsavvslhvywtevxaq4m2u FOREIGN KEY (id,rev) REFERENCES sys_sync_config_a (id,rev)
);

INSERT INTO sys_sync_tree_config (id, start_auto_role_rec)
  SELECT c.id, true  FROM sys_sync_config c WHERE c.id IN (
    SELECT conf.id FROM sys_sync_config conf JOIN sys_system_mapping sm ON(conf.system_mapping_id = sm.id)
    WHERE sm.entity_type = 'TREE'
  );