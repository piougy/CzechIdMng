-- add new intersection table beetween role and identity - guarantee of role
--

CREATE TABLE idm_role_guarantee
(
  id bigint NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  modified timestamp without time zone NOT NULL,
  modifier character varying(255),
  original_creator character varying(255),
  original_modifier character varying(255),
  guarantee_id bigint NOT NULL,
  role_id bigint NOT NULL,
  CONSTRAINT idm_role_guarantee_pkey PRIMARY KEY (id),
  CONSTRAINT fk_idm_role_id FOREIGN KEY (role_id)
      REFERENCES public.idm_role (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,  
  CONSTRAINT fk_idm_guarantee_id FOREIGN KEY (guarantee_id)
      REFERENCES public.idm_identity (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

-- create audit table for imd_role_guarantee
CREATE TABLE idm_role_guarantee_aud
(
  id bigint NOT NULL,
  rev integer NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  creator character varying(255),
  modified timestamp without time zone,
  modifier character varying(255),
  original_creator character varying(255),
  original_modifier character varying(255),
  guarantee_id bigint,
  role_id bigint,
  CONSTRAINT idm_role_guarantee_aud_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_revinfo_rev FOREIGN KEY (rev)
      REFERENCES revinfo (rev) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION	
)
WITH (
  OIDS=FALSE
);

