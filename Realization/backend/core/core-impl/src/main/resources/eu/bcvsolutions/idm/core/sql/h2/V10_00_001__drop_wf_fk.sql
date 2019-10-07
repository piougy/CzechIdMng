--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Remove wf foreign key for identity links - are created before processinstance is defined.

ALTER TABLE act_ru_identitylink DROP CONSTRAINT act_fk_idl_procinst;