--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Tree synchronization config

CREATE TABLE sys_sync_tree_config (
	start_auto_role_rec bool NOT NULL,
	id bytea NOT NULL,
	CONSTRAINT sys_sync_tree_config_pkey PRIMARY KEY (id),
	CONSTRAINT fk5pwoxi6mopl3yr5q5ml1j6h9i FOREIGN KEY (id) REFERENCES sys_sync_config(id)
);

CREATE TABLE sys_sync_tree_config_a (
	id bytea NOT NULL,
	rev int8 NOT NULL,
	start_auto_role_rec bool NULL,
	start_auto_role_rec_m bool NULL,
	CONSTRAINT sys_sync_tree_config_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk3afuuy6ycqwr2fhl8cu8p7g25 FOREIGN KEY (id, rev) REFERENCES sys_sync_config_a(id, rev)
);

INSERT INTO sys_sync_tree_config (id, start_auto_role_rec)
  SELECT c.id, true  FROM sys_sync_config c WHERE c.id IN (
    SELECT conf.id FROM sys_sync_config conf JOIN sys_system_mapping sm ON(conf.system_mapping_id = sm.id)
    WHERE sm.entity_type = 'TREE'
  );