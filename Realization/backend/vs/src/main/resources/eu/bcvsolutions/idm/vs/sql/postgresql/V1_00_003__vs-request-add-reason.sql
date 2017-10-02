-- Add column Reason to VS request

ALTER TABLE vs_request ADD COLUMN reason character varying(2000);
ALTER TABLE vs_request_a ADD COLUMN reason character varying(2000);
ALTER TABLE vs_request_a ADD COLUMN reason_m boolean;