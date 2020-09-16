--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- notification attachment

CREATE TABLE idm_notification_attachment (
	id binary(16) NOT NULL,
	created datetime2(7) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(7) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	name nvarchar(255) NOT NULL,
	attachment_id binary(16) NULL,
	notification_id binary(16) NOT NULL,
	CONSTRAINT idm_notification_attachment_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_notification_att_att ON idm_notification_attachment(attachment_id);
CREATE INDEX idx_idm_notification_att_not ON idm_notification_attachment(notification_id);

