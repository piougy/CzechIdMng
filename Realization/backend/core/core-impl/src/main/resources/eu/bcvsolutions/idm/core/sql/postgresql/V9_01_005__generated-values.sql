--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Entity and audit entity for generated values


CREATE TABLE idm_generated_value (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea,
	modified timestamp,
	modifier varchar(255),
	modifier_id bytea,
	original_creator varchar(255),
	original_creator_id bytea,
	original_modifier varchar(255),
	original_modifier_id binary,
	realm_id binary,
	transaction_id binary,
	entity_type varchar(255) NOT NULL,
	description varchar(2000),
	disabled boolean NOT NULL,
	regenerate_value boolean NOT NULL,
	generator_properties binary,
	generator_type varchar(255) NOT NULL,
	seq smallint,
	CONSTRAINT idm_generated_value_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_generated_val_entity_t
  ON idm_generated_value
  USING btree
  (entity_type);

CREATE TABLE idm_generated_value_a (
	id bytea NOT NULL,
	rev bigint NOT NULL,
	revtype smallint,
	created datetime,
	created_m boolean,
	creator varchar(255),
	creator_m boolean,
	creator_id bytea,
	creator_id_m boolean,
	modifier varchar(255),
	modifier_m boolean,
	modifier_id bytea,
	modifier_id_m boolean,
	original_creator varchar(255),
	original_creator_m boolean,
	original_creator_id bytea,
	original_creator_id_m boolean,
	original_modifier varchar(255),
	original_modifier_m boolean,
	original_modifier_id bytea,
	original_modifier_id_m boolean,
	realm_id bytea,
	realm_id_m boolean,
	transaction_id bytea,
	transaction_id_m boolean,
	description varchar(2000),
	description_m boolean,
	disabled boolean,
	disabled_m boolean,
	regenerate_value boolean,
	regenerate_value_m boolean,
	entity_type varchar(255),
	entity_type_m boolean,
	generator_type varchar(255),
	generator_type_m boolean,
	seq smallint,
	seq_m boolean,
	CONSTRAINT idm_generated_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_42bliu4so7fxlcusukbrtmaj4 FOREIGN KEY (rev)
	REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
