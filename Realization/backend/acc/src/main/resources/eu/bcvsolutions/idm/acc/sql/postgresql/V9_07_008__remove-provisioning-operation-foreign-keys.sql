--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Remove foreign keys to provisioning operation table - #1904

-- provisioning operation
ALTER TABLE sys_provisioning_operation DROP constraint IF EXISTS fk_k1ad29ndiwuldqem93a01c6qx;
ALTER TABLE sys_provisioning_operation DROP constraint IF EXISTS fk_7vyyfd4iyidh411rqdoa3086b;
ALTER TABLE sys_provisioning_operation DROP constraint IF EXISTS fk_reb3mqn6a4f5mxdeipcrhthw9;