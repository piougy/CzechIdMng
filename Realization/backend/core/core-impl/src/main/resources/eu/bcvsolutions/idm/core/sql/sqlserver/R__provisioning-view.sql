-- public view for provisioning

CREATE VIEW identity_role_view AS
 SELECT i.modified,
    i.username,
    i.first_name,
    i.last_name,
    i.title_before,
    i.title_after,
    i.email,
    i.phone,
    r.name AS role_name,
    ir.valid_from,
    ir.valid_till
   FROM idm_identity i,
    idm_role r,
    idm_identity_role ir,
    idm_identity_contract ic
  WHERE i.id = ic.identity_id AND ir.identity_contract_id = ic.id AND r.id = ir.role_id;
