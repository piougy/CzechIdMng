--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- In sync identity config table are records for other sync type (tree, role, role-catalogue), we have to delete them.

DELETE FROM sys_sync_identity_config ssic WHERE ssic.id IN(
  SELECT sci.id FROM sys_system_mapping sm 
    JOIN sys_sync_config sc ON (sm.id = sc.system_mapping_id ) 
    JOIN sys_sync_identity_config sci ON (sc.id = sci.id) 
    WHERE sm.entity_type !=  'IDENTITY'
);




