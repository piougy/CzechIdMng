package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * PhoneIdentityFilter test.
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class PhoneIdentityFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private PhoneIdentityFilter filter;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testFindIdentityByPhone() {
		// prepare data
		IdmIdentityDto identityOne = new IdmIdentityDto(getHelper().createName());
		identityOne.setPhone(getHelper().createName().substring(0, 30));
		identityOne = identityService.save(identityOne);
		IdmIdentityDto identityTwo =  new IdmIdentityDto(getHelper().createName());
		identityTwo.setPhone(getHelper().createName().substring(0, 30));
		identityTwo = identityService.save(identityTwo);
		//
		IdmIdentityFilter dataFilter = new IdmIdentityFilter();
		dataFilter.setPhone(identityOne.getPhone());
		List<IdmIdentity> identities = filter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setPhone(identityTwo.getPhone());
		identities = filter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
	}
}
