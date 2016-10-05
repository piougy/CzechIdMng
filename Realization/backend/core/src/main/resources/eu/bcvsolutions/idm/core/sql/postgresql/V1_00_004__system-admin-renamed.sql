-- SYSTEM_ADMIN is renamed to APP_ADMIN
update idm_role_authority set target_permission = 'APP' where target_permission = 'SYSTEM';