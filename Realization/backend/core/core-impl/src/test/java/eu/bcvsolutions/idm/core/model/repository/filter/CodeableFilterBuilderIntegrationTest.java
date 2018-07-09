package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * UuidFilterBuilder test
 * 
 * @author Radek Tomiška
 *
 */
public class CodeableFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private CodeableFilter<IdmIdentity> identityFilter;

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
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleOne = getHelper().createRole();
		//
		DataFilter dataFilter = new DataFilter(IdmIdentityDto.class);
		dataFilter.setCodeableIdentifier(identityOne.getId().toString());
		List<IdmIdentity> identities = identityFilter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(identityTwo.getId().toString());
		identities = identityFilter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(roleOne.getId().toString());
		assertEquals(0, identityFilter.find(dataFilter, null).getTotalElements());
	}
	
	@Test
	public void testFindIdentityByUsername() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleOne = getHelper().createRole();
		//
		DataFilter dataFilter = new DataFilter(IdmIdentityDto.class);
		dataFilter.setCodeableIdentifier(identityOne.getUsername());
		List<IdmIdentity> identities = identityFilter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(identityTwo.getUsername());
		identities = identityFilter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		//
		dataFilter.setCodeableIdentifier(roleOne.getId().toString());
		assertEquals(0, identityFilter.find(dataFilter, null).getTotalElements());
	}
}
