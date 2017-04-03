package eu.bcvsolutions.idm.core.security.evaluator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.RoleGuaranteeEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Authorization evaluator's unit tests
 * 
 * @author Radek Tomiška
 *
 */
public class AuthorizationEvaluatorUnitTest extends AbstractUnitTest {
	
	@Mock
	private SecurityService securityService;
	
	@Test
	public void testSupportsEvaluatorType() {
		BasePermissionEvaluator e = new BasePermissionEvaluator();
		RoleGuaranteeEvaluator r = new RoleGuaranteeEvaluator(securityService);
		//
		assertTrue(e.supports(BaseEntity.class));
		assertTrue(e.supports(AbstractEntity.class));
		assertTrue(e.supports(IdmRole.class));
		//
		assertFalse(r.supports(BaseEntity.class));
		assertFalse(r.supports(AbstractEntity.class));
		assertTrue(r.supports(IdmRole.class));
	}	
}
