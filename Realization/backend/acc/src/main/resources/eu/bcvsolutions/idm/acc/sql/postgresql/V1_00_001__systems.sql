-- systems and role systems assign

CREATE TABLE acc_role_system (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    type character varying(255),
    role_id bigint NOT NULL,
    system_id bigint NOT NULL
);

CREATE TABLE acc_role_system_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    type character varying(255),
    role_id bigint,
    system_id bigint
);

CREATE TABLE sys_system (
    id bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    modified timestamp without time zone NOT NULL,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    description text,
    disabled boolean NOT NULL,
    name character varying(255) NOT NULL,
    version bigint
);
--

CREATE TABLE sys_system_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    creator character varying(255),
    modified timestamp without time zone,
    modifier character varying(255),
    original_creator character varying(255),
    original_modifier character varying(255),
    description text,
    disabled boolean,
    name character varying(255)
);


ALTER TABLE ONLY acc_role_system_aud ADD CONSTRAINT acc_role_system_aud_pkey PRIMARY KEY (id, rev);
ALTER TABLE ONLY acc_role_system ADD CONSTRAINT acc_role_system_pkey PRIMARY KEY (id);
ALTER TABLE ONLY sys_system_aud ADD CONSTRAINT sys_system_aud_pkey PRIMARY KEY (id, rev);
ALTER TABLE ONLY sys_system ADD CONSTRAINT sys_system_pkey PRIMARY KEY (id);
ALTER TABLE ONLY sys_system ADD CONSTRAINT ux_system_name UNIQUE (name);
ALTER TABLE ONLY acc_role_system ADD CONSTRAINT ux_role_system_type UNIQUE (type, role_id, system_id);

ALTER TABLE ONLY sys_system_aud ADD CONSTRAINT fk_acc_sys_system_aud_revinfo_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);
ALTER TABLE ONLY acc_role_system ADD CONSTRAINT fk_acc_role_system_system_id FOREIGN KEY (system_id) REFERENCES sys_system(id);
ALTER TABLE ONLY acc_role_system ADD CONSTRAINT fk_acc_role_system_role_id FOREIGN KEY (role_id) REFERENCES idm_role(id);

ALTER TABLE ONLY acc_role_system_aud ADD CONSTRAINT fk_acc_role_system_aud_revinfo_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);