--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- External id for vs request, which could be synchronized from external source (e.g. ticket system)

ALTER TABLE vs_request ADD external_id nvarchar(255);
CREATE INDEX idx_vs_request_external_id
  ON vs_request(external_id);
ALTER TABLE vs_request_a ADD external_id nvarchar(255);
ALTER TABLE vs_request_a ADD external_id_m bit;
