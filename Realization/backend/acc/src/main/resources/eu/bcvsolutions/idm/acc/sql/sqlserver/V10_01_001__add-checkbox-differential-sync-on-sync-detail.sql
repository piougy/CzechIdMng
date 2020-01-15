--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- It adds new option on Sync detail page activating differential synchronization functionality - #1954

-- add new boolean item to table
ALTER TABLE sys_sync_config ADD differential_sync bit NOT NULL DEFAULT 0;
ALTER TABLE sys_sync_config_a ADD differential_sync bit;
ALTER TABLE sys_sync_config_a ADD differential_sync_m bit;
