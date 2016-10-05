-- not null to role system type

UPDATE acc_role_system SET type = 'default' WHERE type is null;
ALTER TABLE acc_role_system ALTER type SET NOT NULL;

-- accont management tables

CREATE TABLE acc_account (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    entity_type character varying(255) NOT NULL,
    system_id bigint NOT NULL,
    system_entity_id bigint
);

CREATE TABLE acc_account_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    entity_type character varying(255),
    system_id bigint,
    system_entity_id bigint
);

CREATE TABLE acc_identity_account (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    ownership boolean NOT NULL,
    account_id bigint NOT NULL,
    identity_id bigint NOT NULL,
    role_id bigint
);

CREATE TABLE acc_identity_account_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    ownership boolean,
    account_id bigint,
    identity_id bigint,
    role_id bigint
);

CREATE TABLE sys_system_entity (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    entity_type character varying(255) NOT NULL,
    uid character varying(1000) NOT NULL,
    system_id bigint NOT NULL
);

-- primary keys

ALTER TABLE ONLY acc_account_aud
    ADD CONSTRAINT acc_account_aud_pkey PRIMARY KEY (id, rev);
    
ALTER TABLE ONLY acc_account
    ADD CONSTRAINT acc_account_pkey PRIMARY KEY (id);
    
ALTER TABLE ONLY acc_identity_account_aud
    ADD CONSTRAINT acc_identity_account_aud_pkey PRIMARY KEY (id, rev);
    
ALTER TABLE ONLY acc_identity_account
    ADD CONSTRAINT acc_identity_account_pkey PRIMARY KEY (id);
    
ALTER TABLE ONLY sys_system_entity
    ADD CONSTRAINT sys_system_entity_pkey PRIMARY KEY (id);
    
-- indexes
    
ALTER TABLE ONLY acc_identity_account
    ADD CONSTRAINT ux_identity_account UNIQUE (identity_id, account_id);
    
ALTER TABLE ONLY sys_system_entity
    ADD CONSTRAINT ux_system_entity_type_uid UNIQUE (entity_type, uid);
    
-- foreign keuy
    
ALTER TABLE ONLY acc_identity_account
    ADD CONSTRAINT fk_acc_identity_account_account_id FOREIGN KEY (account_id) REFERENCES acc_account(id) ON DELETE CASCADE;
    
ALTER TABLE ONLY acc_identity_account
    ADD CONSTRAINT fk_acc_identity_account_identity_id FOREIGN KEY (identity_id) REFERENCES idm_identity(id) ON DELETE CASCADE;
    
ALTER TABLE ONLY acc_identity_account
    ADD CONSTRAINT fk_acc_identity_account_role_id FOREIGN KEY (role_id) REFERENCES idm_role(id) ON DELETE SET NULL;
    
ALTER TABLE ONLY sys_system_entity
    ADD CONSTRAINT fk_sys_system_entity_system_id FOREIGN KEY (system_id) REFERENCES sys_system(id) ON DELETE CASCADE;
    
ALTER TABLE ONLY acc_account_aud
    ADD CONSTRAINT fk_acc_account_aud_revinfo_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);
    
ALTER TABLE ONLY acc_account
    ADD CONSTRAINT fk_acc_account_system_entity_id FOREIGN KEY (system_entity_id) REFERENCES sys_system_entity(id) ON DELETE SET NULL;
    
ALTER TABLE ONLY acc_identity_account_aud
    ADD CONSTRAINT fk_acc_identity_account_aud_revinfo_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);
    
ALTER TABLE ONLY acc_account
    ADD CONSTRAINT fk_acc_account_system_id FOREIGN KEY (system_id) REFERENCES sys_system(id) ON DELETE CASCADE;

