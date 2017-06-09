package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * UsernameIdentityFilter test
 * 
 * @author Radek Tomiška
 *
 */
public class UsernameIdentityFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityRepository repository;
	
	@Test
	public void testFindIdentityByUuid() {
		// prepare data
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		UsernameIdentityFilter identityFilter = new UsernameIdentityFilter(repository); 
		//
		IdentityFilter dataFilter = new IdentityFilter();
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
