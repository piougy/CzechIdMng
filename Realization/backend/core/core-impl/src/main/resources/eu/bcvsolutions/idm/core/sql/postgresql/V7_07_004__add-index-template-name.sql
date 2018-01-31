--
-- CzechIdM 7.7 Flyway script
-- BCV solutions s.r.o.
--
-- add index on name in idm_notification_template table
CREATE INDEX idx_idm_n_template_name
  ON idm_notification_template
  USING btree
  (name);
