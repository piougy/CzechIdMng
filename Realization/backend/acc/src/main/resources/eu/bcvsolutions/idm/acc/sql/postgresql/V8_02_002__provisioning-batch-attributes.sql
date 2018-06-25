--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Add attributes about provisioned entity to provisioning batch

-- clean up provisioning queue - remove "skeletons"
delete from sys_provisioning_operation where provisioning_batch_id is null; 
delete from sys_provisioning_operation where provisioning_batch_id not in (select id from sys_provisioning_batch);
delete from sys_provisioning_batch where id not in (select provisioning_batch_id from sys_provisioning_operation);

alter table sys_provisioning_batch add column system_entity_id bytea;

update sys_provisioning_batch set system_entity_id = (select distinct(o.system_entity_id) from sys_provisioning_operation o where sys_provisioning_batch.id = o.provisioning_batch_id);

alter table sys_provisioning_batch alter column system_entity_id set not null;
