package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Base class for identity authorities processor tests. Provides helper methods
 * and access to common fields, services and repositories.
 *  
 * @author Jan Helbich
 *
 */
public abstract class AbstractIdentityAuthoritiesProcessorTest extends AbstractIntegrationTest {
	
	@Autowired
	protected IdmIdentityService identityService;

	@Autowired
	protected IdmRoleService roleService;

	@Autowired
	protected IdmIdentityRoleService identityRoleService;
	
	@Autowired
	protected GrantedAuthoritiesFactory authoritiesFactory;
	
	@Autowired
	protected IdmIdentityContractService contractService;
	
	@Autowired
	protected IdmAuthorityChangeRepository acRepository;
	
	@Autowired
	protected IdmAuthorizationPolicyService authorizationPolicyService;
	
	@PersistenceContext
	protected EntityManager entityManager;
	
	@Before
	public void before() {
		loginAsAdmin("test-authorities-processor-user");
	}
	
	@After
	public void after() {
		logout();
	}
	
	protected IdmIdentityRoleDto getTestIdentityRole(IdmRole role, IdmIdentityContractDto c) {
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

	protected IdmRole getTestRole() {
		IdmRole role = new IdmRole();
		role.setName(UUID.randomUUID().toString());
		role = saveInTransaction(role, roleService);
		getTestPolicy(role);
		return role;
	}
	
	protected IdmAuthorizationPolicyDto getTestPolicy(IdmRole role) {
		return getTestPolicy(role, IdmBasePermission.DELETE, CoreGroupPermission.IDENTITY);
	}
	
	protected IdmAuthorizationPolicyDto getTestPolicy(IdmRole role, BasePermission base, GroupPermission group) {
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		policy.setGroupPermission(group.getName());
		policy.setPermissions(base);
		policy.setRole(role.getId());
		policy.setEvaluator(BasePermissionEvaluator.class);
		return authorizationPolicyService.get(authorizationPolicyService.save(policy).getId());		
	}

	protected IdmIdentityDto getTestUser() {
		IdmIdentityDto i = new IdmIdentityDto();
		i.setUsername("testuser-" + UUID.randomUUID().toString());
		i.setFirstName("Test");
		i.setLastName("User");
		i.setDisabled(false);
		i = saveInTransaction(i, identityService);
		return i;
	}
	
	protected IdmAuthorityChange getAuthorityChange(IdmIdentityDto i) {
		return acRepository.findOneByIdentity_Id(i.getId());
	}

}
