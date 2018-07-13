package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * UsernameIdentityFilter test
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class UsernameIdentityFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private UsernameIdentityFilter filter;

	@Test
	public void testFindIdentityByUuid() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo =  getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityFilter dataFilter = new IdmIdentityFilter();
		dataFilter.setUsername(identityOne.getUsername());
		List<IdmIdentity> identities = filter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setUsername(identityTwo.getUsername());
		identities = filter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
	}
}
