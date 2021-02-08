--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Remote server agenda

CREATE TABLE sys_remote_server (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	host varchar(255) NOT NULL,
	port int4 NULL,
	timeout int4 NULL,
	use_ssl bool NOT NULL,
	description varchar(2000) NULL,
	CONSTRAINT sys_remote_server_pkey PRIMARY KEY (id)
);

CREATE TABLE sys_remote_server_a (
	id bytea NOT NULL,
	rev int8 NOT NULL,
	revtype int2 NULL,
	created timestamp NULL,
	created_m bool NULL,
	creator varchar(255) NULL,
	creator_m bool NULL,
	creator_id bytea NULL,
	creator_id_m bool NULL,
	modifier varchar(255) NULL,
	modifier_m bool NULL,
	modifier_id bytea NULL,
	modifier_id_m bool NULL,
	original_creator varchar(255) NULL,
	original_creator_m bool NULL,
	original_creator_id bytea NULL,
	original_creator_id_m bool NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bool NULL,
	original_modifier_id bytea NULL,
	original_modifier_id_m bool NULL,
	realm_id bytea NULL,
	realm_id_m bool NULL,
	host varchar(255) NULL,
	host_m bool NULL,
	port int4 NULL,
	port_m bool NULL,
	timeout int4 NULL,
	timeout_m bool NULL,
	use_ssl bool NULL,
	use_ssl_m bool NULL,
	description varchar(2000) NULL,
	description_m bool NULL,
	CONSTRAINT sys_remote_server_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fkcadsr1mgp6qgxenj0ikiqn6bg FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

ALTER TABLE sys_system ADD remote_server_id bytea NULL;
ALTER TABLE sys_system_a ADD remote_server_id bytea NULL;
ALTER TABLE sys_system_a ADD remote_server_m bool NULL;

