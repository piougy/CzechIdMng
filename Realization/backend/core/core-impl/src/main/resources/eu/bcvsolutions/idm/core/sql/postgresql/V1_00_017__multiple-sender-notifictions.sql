--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script adds multiple senders for notifications and sms support


----- TABLE idm_notification_websocket -----
CREATE TABLE idm_notification_sms
(
  id bytea NOT NULL,
  CONSTRAINT idm_notification_sms_pkey PRIMARY KEY (id)
);