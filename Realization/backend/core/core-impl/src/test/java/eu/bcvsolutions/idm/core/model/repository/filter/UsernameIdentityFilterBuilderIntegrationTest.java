package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * UsernameIdentityFilter test
 * 
 * @author Radek Tomiška
 *
 */
public class UsernameIdentityFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityRepository repository;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testFindIdentityByUuid() {
		// prepare data
		IdmIdentityDto identityOne = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = helper.createIdentity((GuardedString) null);
		UsernameIdentityFilter identityFilter = new UsernameIdentityFilter(repository); 
		//
		IdmIdentityFilter dataFilter = new IdmIdentityFilter();
		dataFilter.setUsername(identityOne.getUsername());
		List<IdmIdentity> identities = identityFilter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setUsername(identityTwo.getUsername());
		identities = identityFilter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
	}
}
