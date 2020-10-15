--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- notification attachment

CREATE TABLE idm_notification_attachment (
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
	name varchar(255) NOT NULL,
	attachment_id bytea NULL,
	notification_id bytea NOT NULL,
	CONSTRAINT idm_notification_attachment_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_notification_att_att ON idm_notification_attachment USING btree (attachment_id);
CREATE INDEX idx_idm_notification_att_not ON idm_notification_attachment USING btree (notification_id);

