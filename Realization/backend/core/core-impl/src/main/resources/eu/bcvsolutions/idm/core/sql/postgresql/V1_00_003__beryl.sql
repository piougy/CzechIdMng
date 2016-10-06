-- Html message column extend size (on TEXT) (remove previous messages)
ALTER TABLE idm_notification DROP COLUMN html_message;
ALTER TABLE idm_notification ADD COLUMN html_message TEXT;