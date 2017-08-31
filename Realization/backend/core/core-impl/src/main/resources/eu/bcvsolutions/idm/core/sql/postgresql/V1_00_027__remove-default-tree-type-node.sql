--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- remove default tree type and node from tree type (flags) - configuration service is used for persist default tree type and node.

-- move data to new configuration properties
insert into idm_configuration(id, created, creator, name, value, secured, confidential) values (
	decode(md5(random()::text || clock_timestamp()::text), 'hex'),
	now(),
	'[SYSTEM]',
	'idm.sec.core.tree.defaultType', 
	(select encode(id, 'hex')::uuid from idm_tree_type where default_tree_type = true),
	true,
	false);
insert into idm_configuration(id, created, creator, name, value, secured, confidential) values (
	decode(md5(random()::text || clock_timestamp()::text), 'hex'),
	now(),
	'[SYSTEM]',
	'idm.sec.core.tree.defaultNode', 
	(select encode(default_tree_node_id, 'hex')::uuid from idm_tree_type where default_tree_type = true),
	true,
	false);

-- ddl
alter table idm_tree_type drop column default_tree_type;
alter table idm_tree_type drop column default_tree_node_id;
alter table idm_tree_type_a drop column default_tree_type;
alter table idm_tree_type_a drop column default_tree_node_id;
alter table idm_tree_type_a drop column default_tree_type_m;
alter table idm_tree_type_a drop column default_tree_node_m;


