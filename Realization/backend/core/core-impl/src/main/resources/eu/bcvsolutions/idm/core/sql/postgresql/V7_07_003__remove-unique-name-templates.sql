--
-- CzechIdM 7.7 Flyway script
-- BCV solutions s.r.o.
--
-- Drop unique index on name in idm_notification_template table
ALTER TABLE idm_notification_template DROP CONSTRAINT ux_idm_notification_template_name;
