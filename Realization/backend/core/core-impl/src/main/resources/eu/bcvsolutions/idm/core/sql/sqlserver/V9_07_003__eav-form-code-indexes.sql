--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add indexex to form definition and attribute codes

CREATE INDEX idx_idm_form_definition_code ON idm_form_definition (code);
CREATE INDEX idx_idm_f_a_code ON idm_form_attribute (code);