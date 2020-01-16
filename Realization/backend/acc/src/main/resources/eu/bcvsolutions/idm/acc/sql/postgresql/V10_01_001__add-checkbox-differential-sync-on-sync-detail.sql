--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- It adds new option on Sync detail page activating differential synchronization functionality - #1954

-- add new boolean item to table
ALTER TABLE sys_sync_config ADD COLUMN differential_sync boolean NOT NULL DEFAULT False;
ALTER TABLE sys_sync_config_a ADD COLUMN differential_sync boolean;
ALTER TABLE sys_sync_config_a ADD COLUMN differential_sync_m boolean;
