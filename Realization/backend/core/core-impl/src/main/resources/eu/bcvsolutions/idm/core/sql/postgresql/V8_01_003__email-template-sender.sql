--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Email template sender

ALTER TABLE idm_notification_template ADD COLUMN sender character varying(255);
ALTER TABLE idm_notification_template_a ADD COLUMN sender character varying(255);
ALTER TABLE idm_notification_template_a ADD COLUMN sender_m boolean;
