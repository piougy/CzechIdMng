-- add tree type code
ALTER TABLE idm_tree_type ADD COLUMN code character varying(255);
ALTER TABLE idm_tree_type_aud ADD COLUMN code character varying(255);
UPDATE idm_tree_type SET code = name;
ALTER TABLE idm_tree_type ALTER code SET NOT NULL;
ALTER TABLE ONLY idm_tree_type ADD CONSTRAINT ux_tree_type_code UNIQUE (code);

-- add identity contract string position - if working position tree is not configured, then this string could be used
ALTER TABLE idm_identity_contract ADD COLUMN position character varying(255);