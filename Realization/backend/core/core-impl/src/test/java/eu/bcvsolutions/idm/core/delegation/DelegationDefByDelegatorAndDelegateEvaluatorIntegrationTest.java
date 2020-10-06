package eu.bcvsolutions.idm.core.delegation;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.delegation.DelegationDefByDelegatorAndDelegateEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SubordinatesEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permission to delegation definition.
 *
 * @author Vít Švanda
 */
@Transactional
public class DelegationDefByDelegatorAndDelegateEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired
	private IdmDelegationDefinitionService service;

	@Test(expected = ForbiddenEntityException.class)
	public void testCreateDelegationNoPolicy() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto delegate = getHelper().createIdentity();

		try {
			getHelper().login(identity);
			getHelper().createDelegation(delegate, identity, IdmBasePermission.CREATE);
		} finally {
			logout();
		}
	}

	@Test(expected = ForbiddenEntityException.class)
	public void testCreateDelegationFailed() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto delegate = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);

		// Create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegationDefinition.class,
				DelegationDefByDelegatorAndDelegateEvaluator.class,
				IdmBasePermission.CREATE);

		try {
			getHelper().login(identity);
			getHelper().createDelegation(delegate, identity, IdmBasePermission.CREATE);
		} finally {
			logout();
		}
	}

	@Test
	public void testCreateDelegation() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto delegate = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);

		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SelfIdentityEvaluator.class,
				IdentityBasePermission.DELEGATOR);

		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				BasePermissionEvaluator.class,
				IdentityBasePermission.DELEGATE);

		// Create authorization policy - for check permission for delegate and delegator.
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegationDefinition.class,
				DelegationDefByDelegatorAndDelegateEvaluator.class,
				IdmBasePermission.CREATE);

		try {
			getHelper().login(identity);
			getHelper().createDelegation(delegate, identity, IdmBasePermission.CREATE);
		} finally {
			logout();
		}
	}

	@Test(expected = ForbiddenEntityException.class)
	public void testCreateDelegationDelegateNotGranted() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto delegate = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);

		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SelfIdentityEvaluator.class,
				IdentityBasePermission.DELEGATOR);

		// Delegate is not self identity -> expected exception.
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SelfIdentityEvaluator.class,
				IdentityBasePermission.DELEGATE);

		// Create authorization policy - for check permission for delegate and delegator.
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegationDefinition.class,
				DelegationDefByDelegatorAndDelegateEvaluator.class,
				IdmBasePermission.CREATE);

		try {
			getHelper().login(identity);
			getHelper().createDelegation(delegate, identity, IdmBasePermission.CREATE);
		} finally {
			logout();
		}
	}

	@Test(expected = ForbiddenEntityException.class)
	public void testCreateDelegationDelegatorNotGranted() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto delegate = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);

		// Delegator is subordinate -> expected exception.
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SubordinatesEvaluator.class,
				IdentityBasePermission.DELEGATOR);

		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				BasePermissionEvaluator.class,
				IdentityBasePermission.DELEGATE);

		// Create authorization policy - for check permission for delegate and delegator.
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.DELEGATIONDEFINITION,
				IdmDelegationDefinition.class,
				DelegationDefByDelegatorAndDelegateEvaluator.class,
				IdmBasePermission.CREATE);

		try {
			getHelper().login(identity);
			getHelper().createDelegation(delegate, identity, IdmBasePermission.CREATE);
		} finally {
			logout();
		}
	}
}
