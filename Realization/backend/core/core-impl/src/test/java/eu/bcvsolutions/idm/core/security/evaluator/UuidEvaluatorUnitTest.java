package eu.bcvsolutions.idm.core.security.evaluator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Authorization evaluator's unit tests
 * 
 * @author Radek Tomiška
 *
 */
public class UuidEvaluatorUnitTest extends AbstractUnitTest {
	
	private UuidEvaluator evaluator = new UuidEvaluator();
	
	@Test
	public void testSupportsType() {
		assertFalse(evaluator.supports(BaseEntity.class));
		assertTrue(evaluator.supports(AbstractEntity.class));
		assertTrue(evaluator.supports(IdmRole.class));
	}
	
	@Test
	public void testEvaluateReadOnly() {		
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		UUID uuid = UUID.randomUUID();
		IdmRole authorizable = new IdmRole(uuid);
		policy.getEvaluatorProperties().put(UuidEvaluator.PARAMETER_UUID, uuid);
		policy.setPermissions(IdmBasePermission.READ);
		//
		assertTrue(evaluator.evaluate(policy, authorizable, IdmBasePermission.READ));
		assertFalse(evaluator.evaluate(policy, authorizable, IdmBasePermission.UPDATE));
		assertFalse(evaluator.evaluate(policy, authorizable, IdmBasePermission.ADMIN));
	}
	
	@Test
	public void testEvaluateAdminPermission() {		
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		UUID uuid = UUID.randomUUID();
		IdmRole authorizable = new IdmRole(uuid);
		policy.getEvaluatorProperties().put(UuidEvaluator.PARAMETER_UUID, uuid);
		policy.setPermissions(IdmBasePermission.ADMIN);
		//
		assertTrue(evaluator.evaluate(policy, authorizable, IdmBasePermission.READ));
		assertTrue(evaluator.evaluate(policy, authorizable, IdmBasePermission.UPDATE));
		assertTrue(evaluator.evaluate(policy, authorizable, IdmBasePermission.ADMIN));
	}
}
	
