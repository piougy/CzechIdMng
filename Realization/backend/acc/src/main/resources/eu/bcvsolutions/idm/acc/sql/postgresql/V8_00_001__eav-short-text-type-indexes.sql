--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- add new persistent type fot indexed short text

ALTER TABLE sys_system_form_value ADD COLUMN short_text_value character varying(2000);
ALTER TABLE sys_system_form_value_a ADD COLUMN short_text_value character varying(2000);
ALTER TABLE sys_system_form_value_a ADD COLUMN short_text_value_m boolean;

CREATE INDEX idx_sys_sys_form_stxt
  ON sys_system_form_value
  USING btree
  (short_text_value);
CREATE INDEX idx_sys_sys_form_uuid
  ON sys_system_form_value
  USING btree
  (uuid_value);
