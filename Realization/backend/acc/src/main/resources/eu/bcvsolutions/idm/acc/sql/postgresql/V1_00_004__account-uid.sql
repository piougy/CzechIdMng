-- add account uid

ALTER TABLE acc_account ADD COLUMN uid character varying(1000);
UPDATE acc_account SET uid = id;
ALTER TABLE acc_account ALTER uid SET NOT NULL;

ALTER TABLE acc_account_aud ADD COLUMN uid character varying(1000);

-- rename account type

ALTER TABLE acc_account RENAME COLUMN entity_type TO account_type;
ALTER TABLE acc_account_aud RENAME COLUMN entity_type TO account_type;

-- unique indexes on account

ALTER TABLE ONLY acc_account ADD CONSTRAINT ux_account_system_entity UNIQUE (system_entity_id);
ALTER TABLE ONLY acc_account ADD CONSTRAINT ux_account_uid UNIQUE (uid,system_id);



