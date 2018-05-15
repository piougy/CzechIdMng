package eu.bcvsolutions.idm.acc;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.test.AccTestHelper;

/**
 * Acc / Provisioning test helper
 * 
 * @author Radek Tomiška
 */
@Primary
@Component("accTestHelper")
public class DefaultAccTestHelper extends AccTestHelper {
	
	@Autowired private EntityManager entityManager;
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TestResource findResource(String uid) {
		return entityManager.find(TestResource.class, uid);
	}
}
