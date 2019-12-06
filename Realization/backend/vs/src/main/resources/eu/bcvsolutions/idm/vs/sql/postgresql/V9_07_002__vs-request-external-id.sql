--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- External id for vs request, which could be synchronized from external source (e.g. ticket system)

ALTER TABLE vs_request ADD COLUMN external_id character varying(255);
CREATE INDEX idx_vs_request_external_id
  ON vs_request
  USING btree
  (external_id);
ALTER TABLE vs_request_a ADD COLUMN external_id character varying(255);
ALTER TABLE vs_request_a ADD COLUMN external_id_m boolean;
