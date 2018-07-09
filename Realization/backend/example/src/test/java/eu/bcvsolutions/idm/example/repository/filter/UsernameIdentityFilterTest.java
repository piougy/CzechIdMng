package eu.bcvsolutions.idm.example.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for {@link UsernameIdentityFilter}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Transactional
public class UsernameIdentityFilterTest extends AbstractIntegrationTest {

	@Autowired
	private IdmIdentityService identityService;

	@Before
	public void before() {
		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After
	public void after() {
		super.logout();
	}
	
	@Test
	public void testFilteringFound() {
		String username = getHelper().createName();
		IdmIdentityDto identityOne = getHelper().createIdentity(username);
		getHelper().createIdentity(getHelper().createName() + username + getHelper().createName());
		getHelper().createIdentity(getHelper().createName() + username + getHelper().createName());

		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setUsername(username);
		List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();

		// UsernameIdentityFilter filter has order 10 -> not override default filter
		assertEquals(1, identities.size());

		IdmIdentityDto identity = identities.stream().filter(ident -> ident.getId().equals(identityOne.getId())).findFirst().get();
		assertNotNull(identity);
	}

	@Test
	public void testFilteringNotFound() {
		String username = "usernameValue" + System.currentTimeMillis();
		getHelper().createIdentity(username);
		getHelper().createIdentity("123" + username + getHelper().createName());
		getHelper().createIdentity(getHelper().createName() + username + getHelper().createName());

		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setUsername("username1Value"); // value is different than variable username
		List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();

		assertEquals(0, identities.size());
	}
}
