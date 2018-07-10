package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Base class for identity authorities processor tests. Provides helper methods
 * and access to common fields, services and repositories.
 *  
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 */
public abstract class AbstractIdentityAuthoritiesProcessorTest extends AbstractIntegrationTest {
	
	@Autowired protected IdmIdentityService identityService;
	@Autowired protected IdmRoleService roleService;
	@Autowired protected IdmIdentityRoleService identityRoleService;
	@Autowired protected GrantedAuthoritiesFactory authoritiesFactory;
	@Autowired protected IdmIdentityContractService contractService;
	@Autowired protected IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired protected TokenManager tokenManager;
	//
	@PersistenceContext protected EntityManager entityManager;
	
	protected IdmIdentityRoleDto getTestIdentityRole(IdmRoleDto role, IdmIdentityContractDto c) {
		IdmIdentityRoleDto ir = new IdmIdentityRoleDto();
		ir.setIdentityContract(c.getId());
		ir.setRole(role.getId());
		return saveInTransaction(ir, identityRoleService);
	}

	protected IdmIdentityContractDto getTestContract(IdmIdentityDto i) {
		IdmIdentityContractDto c = new IdmIdentityContractDto();
		c.setExterne(false);
		c.setIdentity(i.getId());
		return saveInTransaction(c, contractService);
	}

	protected IdmRoleDto getTestRole() {
		IdmRoleDto role = new IdmRoleDto();
		role.setName(UUID.randomUUID().toString());
		role = saveInTransaction(role, roleService);
		createTestPolicy(role);
		return role;
	}
	
	protected IdmAuthorizationPolicyDto createTestPolicy(IdmRoleDto role) {
		return createTestPolicy(role, IdmBasePermission.DELETE, CoreGroupPermission.IDENTITY);
	}
	
	protected IdmAuthorizationPolicyDto createTestPolicy(IdmRoleDto role, BasePermission base, GroupPermission group) {
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setGroupPermission(group.getName());
		policy.setPermissions(base);
		policy.setRole(role.getId());
		policy.setEvaluator(BasePermissionEvaluator.class);
		return authorizationPolicyService.get(authorizationPolicyService.save(policy).getId());		
	}
}
