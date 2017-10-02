--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- add audit fields

ALTER TABLE vs_request_a ADD COLUMN system_m boolean;
ALTER TABLE vs_request_a ADD COLUMN duplicate_to_request_id bytea;
ALTER TABLE vs_request_a ADD COLUMN duplicate_to_request_m boolean;
ALTER TABLE vs_request_a ADD COLUMN previous_request_id bytea;
ALTER TABLE vs_request_a ADD COLUMN previous_request_m boolean;
