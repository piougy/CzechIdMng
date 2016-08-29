-- public view for provisioning

CREATE OR REPLACE VIEW identity_role_view 
AS SELECT i.modified, i.username, i.first_name, i.last_name, i.title_before, i.title_after, i.email, i.phone, r.name AS role_name, ir.valid_from, ir.valid_till 
	FROM idm_identity as i, idm_role as r, idm_identity_role as ir 
	WHERE i.id = ir.identity_id and r.id = ir.role_id;
	
GRANT SELECT ON TABLE identity_role_view TO public;