--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- update product provided AD connector

-- update connector definition
UPDATE idm_form_definition SET code = 'connId:net.tirasa.connid.bundles.ad.ADConnector:net.tirasa.connid.bundles.ad:1.3.4.27', name = 'connId:net.tirasa.connid.bundles.ad.ADConnector:net.tirasa.connid.bundles.ad:1.3.4.27' 
WHERE code = 'connId:net.tirasa.connid.bundles.ad.ADConnector:net.tirasa.connid.bundles.ad:1.3.4.25'
AND NOT EXISTS (SELECT 1 FROM idm_form_definition WHERE code = 'connId:net.tirasa.connid.bundles.ad.ADConnector:net.tirasa.connid.bundles.ad:1.3.4.27');

-- set new connector version for locally (~prosuct) configured systems 
UPDATE sys_system SET connector_bundle_version = '1.3.4.27' 
WHERE connector_bundle_version = '1.3.4.25' 
AND connector_name = 'net.tirasa.connid.bundles.ad.ADConnector'
AND connector_bundle_name = 'net.tirasa.connid.bundles.ad'
AND connector_framework = 'connId'
AND remote = 0;